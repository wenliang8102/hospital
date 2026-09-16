package com.hospital.his.outpatient.application;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.outpatient.persistence.mapper.MedicalRecordMapper;
import com.hospital.his.outpatient.persistence.mapper.OutpatientMapper;
import com.hospital.his.outpatient.persistence.model.MedicalRecordDraft;
import com.hospital.his.outpatient.persistence.model.MedicalRecordRow;
import com.hospital.his.outpatient.persistence.model.PatientQueueRow;
import com.hospital.his.outpatient.web.dto.DiseaseView;
import com.hospital.his.outpatient.web.dto.MedicalRecordRequest;
import com.hospital.his.outpatient.web.dto.MedicalRecordView;
import com.hospital.his.outpatient.web.dto.PatientView;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutpatientService {
    private final OutpatientMapper outpatientMapper;
    private final MedicalRecordMapper medicalRecordMapper;

    public OutpatientService(OutpatientMapper outpatientMapper, MedicalRecordMapper medicalRecordMapper) {
        this.outpatientMapper = outpatientMapper;
        this.medicalRecordMapper = medicalRecordMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<PatientView> searchPatients(
            Long employeeId, String keyword, String state, PageQuery page) {
        String normalizedKeyword = trimToNull(keyword);
        String normalizedState = trimToNull(state);
        long total = outpatientMapper.countPatients(employeeId, normalizedKeyword, normalizedState);
        List<PatientView> items = total == 0 ? List.of() : outpatientMapper
                .searchPatients(employeeId, normalizedKeyword, normalizedState, page.offset(), page.size()).stream()
                .map(PatientView::from)
                .toList();
        return PageResult.of(items, page, total);
    }

    @Transactional
    public PatientView accept(Long registrationId, Long employeeId) {
        PatientQueueRow patient = requirePatient(registrationId, employeeId);
        if (!"REGISTERED".equals(patient.state())) {
            throw invalidState("只有待诊患者可以接诊");
        }
        if (outpatientMapper.transitionState(registrationId, employeeId, "REGISTERED", "IN_CONSULTATION") != 1) {
            throw invalidState("患者状态已发生变化，请刷新后重试");
        }
        return PatientView.from(requirePatient(registrationId, employeeId));
    }

    @Transactional(readOnly = true)
    public MedicalRecordView getMedicalRecord(Long registrationId, Long employeeId) {
        requirePatient(registrationId, employeeId);
        MedicalRecordRow record = medicalRecordMapper.findByRegistrationId(registrationId)
                .orElseThrow(() -> new BusinessException(
                        "RESOURCE_NOT_FOUND", "该患者尚未建立病历", HttpStatus.NOT_FOUND));
        return toView(record);
    }

    @Transactional
    public MedicalRecordView saveMedicalRecord(
            Long registrationId, Long employeeId, MedicalRecordRequest request) {
        PatientQueueRow patient = requirePatient(registrationId, employeeId);
        if (!"IN_CONSULTATION".equals(patient.state())) {
            throw invalidState("只有接诊中的患者可以编辑病历");
        }

        List<Long> diseaseIds = request.diseaseIds().stream().distinct().toList();
        if (!diseaseIds.isEmpty() && medicalRecordMapper.countActiveDiseases(diseaseIds) != diseaseIds.size()) {
            throw new BusinessException("VALIDATION_ERROR", "诊断中包含不存在或已停用的疾病");
        }

        MedicalRecordDraft draft = new MedicalRecordDraft(registrationId,
                trimToNull(request.chiefComplaint()), trimToNull(request.presentIllness()),
                trimToNull(request.presentTreatment()), trimToNull(request.pastHistory()),
                trimToNull(request.allergyHistory()), trimToNull(request.physicalExamination()),
                trimToNull(request.examinationProposal()), trimToNull(request.precaution()),
                trimToNull(request.diagnosis()), trimToNull(request.treatmentPlan()));
        var existing = medicalRecordMapper.findByRegistrationId(registrationId);
        Long medicalRecordId;
        if (existing.isPresent()) {
            medicalRecordMapper.update(draft);
            medicalRecordId = existing.get().id();
        } else {
            medicalRecordMapper.insert(draft);
            medicalRecordId = draft.getId();
        }
        medicalRecordMapper.deleteDiseases(medicalRecordId);
        if (!diseaseIds.isEmpty()) {
            medicalRecordMapper.insertDiseases(medicalRecordId, diseaseIds);
        }
        return toView(medicalRecordMapper.findByRegistrationId(registrationId).orElseThrow());
    }

    @Transactional
    public PatientView complete(Long registrationId, Long employeeId) {
        PatientQueueRow patient = requirePatient(registrationId, employeeId);
        if (!"IN_CONSULTATION".equals(patient.state())) {
            throw invalidState("只有接诊中的患者可以完成看诊");
        }
        MedicalRecordRow record = medicalRecordMapper.findByRegistrationId(registrationId)
                .orElseThrow(() -> invalidState("请先保存病历和最终诊断"));
        if (medicalRecordMapper.findDiseases(record.id()).isEmpty()) {
            throw invalidState("请至少选择一个最终诊断");
        }
        if (outpatientMapper.transitionState(registrationId, employeeId, "IN_CONSULTATION", "COMPLETED") != 1) {
            throw invalidState("患者状态已发生变化，请刷新后重试");
        }
        return PatientView.from(requirePatient(registrationId, employeeId));
    }

    private PatientQueueRow requirePatient(Long registrationId, Long employeeId) {
        return outpatientMapper.findPatient(registrationId, employeeId)
                .orElseThrow(() -> new BusinessException(
                        "RESOURCE_NOT_FOUND", "患者不存在或不属于当前医生", HttpStatus.NOT_FOUND));
    }

    private MedicalRecordView toView(MedicalRecordRow row) {
        List<DiseaseView> diseases = medicalRecordMapper.findDiseases(row.id()).stream()
                .map(DiseaseView::from)
                .toList();
        return MedicalRecordView.from(row, diseases);
    }

    private BusinessException invalidState(String message) {
        return new BusinessException("INVALID_STATE_TRANSITION", message, HttpStatus.CONFLICT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
