package com.hospital.his.outpatient.application;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.outpatient.persistence.mapper.ClinicalOrderMapper;
import com.hospital.his.outpatient.persistence.mapper.OutpatientMapper;
import com.hospital.his.outpatient.persistence.model.MedicalOrderDraft;
import com.hospital.his.outpatient.persistence.model.PrescriptionDraft;
import com.hospital.his.outpatient.web.dto.CreateMedicalOrderRequest;
import com.hospital.his.outpatient.web.dto.CreatePrescriptionRequest;
import com.hospital.his.outpatient.web.dto.MedicalOrderView;
import com.hospital.his.outpatient.web.dto.PrescriptionView;
import com.hospital.his.registration.application.ChargeItemService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicalOrderService {
    private final OutpatientMapper outpatientMapper;
    private final ClinicalOrderMapper orderMapper;
    private final ChargeItemService chargeItemService;

    public ClinicalOrderService(OutpatientMapper outpatientMapper, ClinicalOrderMapper orderMapper,
                                ChargeItemService chargeItemService) {
        this.outpatientMapper = outpatientMapper;
        this.orderMapper = orderMapper;
        this.chargeItemService = chargeItemService;
    }

    @Transactional(readOnly = true)
    public List<MedicalOrderView> medicalOrders(Long registrationId, Long employeeId) {
        requirePatient(registrationId, employeeId);
        return orderMapper.findMedicalOrders(registrationId).stream().map(MedicalOrderView::from).toList();
    }

    @Transactional
    public List<MedicalOrderView> createMedicalOrders(
            Long registrationId, Long employeeId, List<CreateMedicalOrderRequest> requests) {
        requireConsultingPatient(registrationId, employeeId);
        for (CreateMedicalOrderRequest request : requests) {
            var reference = orderMapper.findMedicalTechnology(request.medicalTechnologyId())
                    .orElseThrow(() -> new BusinessException(
                            "RESOURCE_NOT_FOUND", "医技项目不存在或已停用", HttpStatus.NOT_FOUND));
            if (!reference.type().equals(request.type())) {
                throw new BusinessException("VALIDATION_ERROR", "医技项目类型与申请类型不匹配");
            }
            MedicalOrderDraft draft = new MedicalOrderDraft(registrationId, reference.id(),
                    trimToNull(request.requestInfo()), trimToNull(request.bodyPosition()),
                    trimToNull(request.remark()));
            insertOrder(request.type(), draft);
            chargeItemService.create(registrationId, request.type(), draft.getId(), reference.name(),
                    reference.price(), 1);
        }
        return medicalOrders(registrationId, employeeId);
    }

    @Transactional
    public void cancelMedicalOrder(Long registrationId, Long employeeId, String type, Long orderId) {
        requireConsultingPatient(registrationId, employeeId);
        int changed = switch (type) {
            case "CHECK" -> orderMapper.cancelCheck(orderId, registrationId);
            case "INSPECTION" -> orderMapper.cancelInspection(orderId, registrationId);
            case "DISPOSAL" -> orderMapper.cancelDisposal(orderId, registrationId);
            default -> throw new BusinessException("VALIDATION_ERROR", "不支持的医技申请类型");
        };
        if (changed != 1 || orderMapper.voidCharge(type, orderId) != 1) {
            throw invalidState("只有未缴费的医技申请可以作废");
        }
    }

    @Transactional(readOnly = true)
    public List<PrescriptionView> prescriptions(Long registrationId, Long employeeId) {
        requirePatient(registrationId, employeeId);
        return orderMapper.findPrescriptions(registrationId).stream().map(PrescriptionView::from).toList();
    }

    @Transactional
    public List<PrescriptionView> createPrescriptions(
            Long registrationId, Long employeeId, List<CreatePrescriptionRequest> requests) {
        requireConsultingPatient(registrationId, employeeId);
        for (CreatePrescriptionRequest request : requests) {
            var drug = orderMapper.findDrug(request.drugId())
                    .orElseThrow(() -> new BusinessException(
                            "RESOURCE_NOT_FOUND", "药品不存在或已停用", HttpStatus.NOT_FOUND));
            PrescriptionDraft draft = new PrescriptionDraft(
                    registrationId, drug.id(), request.drugUsage().trim(), request.drugNumber());
            orderMapper.insertPrescription(draft);
            String itemName = drug.name() + (drug.format() == null ? "" : " " + drug.format());
            chargeItemService.create(registrationId, "PRESCRIPTION", draft.getId(), itemName,
                    drug.price(), request.drugNumber());
        }
        return prescriptions(registrationId, employeeId);
    }

    @Transactional
    public void cancelPrescription(Long registrationId, Long employeeId, Long prescriptionId) {
        requireConsultingPatient(registrationId, employeeId);
        if (orderMapper.cancelPrescription(prescriptionId, registrationId) != 1
                || orderMapper.voidCharge("PRESCRIPTION", prescriptionId) != 1) {
            throw invalidState("只有未缴费的处方可以作废");
        }
    }

    private void insertOrder(String type, MedicalOrderDraft draft) {
        switch (type) {
            case "CHECK" -> orderMapper.insertCheck(draft);
            case "INSPECTION" -> orderMapper.insertInspection(draft);
            case "DISPOSAL" -> orderMapper.insertDisposal(draft);
            default -> throw new BusinessException("VALIDATION_ERROR", "不支持的医技申请类型");
        }
    }

    private void requireConsultingPatient(Long registrationId, Long employeeId) {
        var patient = requirePatient(registrationId, employeeId);
        if (!"IN_CONSULTATION".equals(patient.state())) {
            throw invalidState("只有接诊中的患者可以开立或作废医嘱");
        }
    }

    private com.hospital.his.outpatient.persistence.model.PatientQueueRow requirePatient(
            Long registrationId, Long employeeId) {
        return outpatientMapper.findPatient(registrationId, employeeId)
                .orElseThrow(() -> new BusinessException(
                        "RESOURCE_NOT_FOUND", "患者不存在或不属于当前医生", HttpStatus.NOT_FOUND));
    }

    private BusinessException invalidState(String message) {
        return new BusinessException("INVALID_STATE_TRANSITION", message, HttpStatus.CONFLICT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
