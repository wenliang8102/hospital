package com.hospital.his.registration.application;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.registration.persistence.mapper.RegistrationMapper;
import com.hospital.his.registration.persistence.model.RegistrationDraft;
import com.hospital.his.registration.persistence.model.RegistrationReferenceRow;
import com.hospital.his.registration.web.dto.CreateRegistrationRequest;
import com.hospital.his.registration.web.dto.RegistrationView;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class RegistrationService {
    private static final DateTimeFormatter CASE_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final RegistrationMapper mapper;
    private final Clock clock;

    @Autowired
    public RegistrationService(RegistrationMapper mapper) {
        this(mapper, Clock.systemDefaultZone());
    }

    RegistrationService(RegistrationMapper mapper, Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    public String generateCaseNumber() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return "H" + LocalDate.now(clock).format(CASE_DATE) + suffix;
    }

    @Transactional
    public RegistrationView create(CreateRegistrationRequest request) {
        var existing = mapper.findByRequestNo(request.requestId());
        if (existing.isPresent()) {
            return RegistrationView.from(existing.get());
        }

        validateVisitDate(request.visitDate());
        RegistrationReferenceRow reference = mapper.lockRegistrationReference(request.employeeId())
                .orElseThrow(() -> new BusinessException("RESOURCE_NOT_FOUND", "医生不存在或当前不可挂号", HttpStatus.NOT_FOUND));

        existing = mapper.findByRequestNo(request.requestId());
        if (existing.isPresent()) {
            return RegistrationView.from(existing.get());
        }
        if (!reference.departmentId().equals(request.departmentId())) {
            throw new BusinessException("VALIDATION_ERROR", "所选医生不属于该科室");
        }
        if (!reference.registrationLevelId().equals(request.registrationLevelId())) {
            throw new BusinessException("VALIDATION_ERROR", "所选医生与挂号级别不匹配");
        }
        if (!mapper.existsActiveSettlementCategory(request.settlementCategoryId())) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "结算类别不存在或已停用", HttpStatus.NOT_FOUND);
        }

        LocalDate visitDay = request.visitDate().toLocalDate();
        long occupied = mapper.countOccupiedSlot(
                request.employeeId(), visitDay.atStartOfDay(), visitDay.plusDays(1).atStartOfDay(), request.noon());
        if (occupied >= reference.registrationQuota()) {
            throw new BusinessException("REGISTRATION_QUOTA_EXCEEDED", "该医生当前时段号额已满", HttpStatus.CONFLICT);
        }

        LocalDateTime normalizedVisitTime = request.visitDate().toLocalDate().atTime(
                "AM".equals(request.noon()) ? LocalTime.of(8, 0) : LocalTime.of(13, 0));
        RegistrationDraft draft = new RegistrationDraft(
                request.requestId(), request.caseNumber().trim(), request.realName().trim(), request.gender(),
                trimToNull(request.cardNumber()), request.birthday(), request.age(), request.ageType(),
                trimToNull(request.homeAddress()), normalizedVisitTime, request.noon(), request.departmentId(),
                request.employeeId(), request.registrationLevelId(), request.settlementCategoryId(),
                request.booked(), request.registrationMethod(), reference.registrationFee());
        mapper.insert(draft);
        mapper.insertRegistrationCharge(
                draft.getId(), reference.registrationLevelName() + "挂号费", reference.registrationFee());
        return get(draft.getId());
    }

    @Transactional(readOnly = true)
    public RegistrationView get(Long id) {
        return mapper.findById(id)
                .map(RegistrationView::from)
                .orElseThrow(() -> new BusinessException("RESOURCE_NOT_FOUND", "挂号记录不存在", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResult<RegistrationView> search(String keyword, String state, PageQuery page) {
        String normalizedKeyword = trimToNull(keyword);
        String normalizedState = trimToNull(state);
        long total = mapper.count(normalizedKeyword, normalizedState);
        var items = total == 0 ? java.util.List.<RegistrationView>of() : mapper
                .search(normalizedKeyword, normalizedState, page.offset(), page.size()).stream()
                .map(RegistrationView::from)
                .toList();
        return PageResult.of(items, page, total);
    }

    @Transactional
    public void cancel(Long id) {
        RegistrationView registration = get(id);
        if (!"REGISTERED".equals(registration.state())) {
            throw new BusinessException("INVALID_STATE_TRANSITION", "只有待诊挂号可以退号", HttpStatus.CONFLICT);
        }
        if (mapper.countPaidChargeItems(id) > 0) {
            throw new BusinessException("INVALID_STATE_TRANSITION", "存在已缴费项目，请先完成退费", HttpStatus.CONFLICT);
        }
        if (mapper.transitionState(id, "REGISTERED", "CANCELLED") != 1) {
            throw new BusinessException("INVALID_STATE_TRANSITION", "挂号状态已发生变化，请刷新后重试", HttpStatus.CONFLICT);
        }
        mapper.voidUnpaidChargeItems(id);
    }

    private void validateVisitDate(LocalDateTime visitDate) {
        if (visitDate.toLocalDate().isBefore(LocalDate.now(clock))) {
            throw new BusinessException("VALIDATION_ERROR", "就诊日期不能早于今天");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
