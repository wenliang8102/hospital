package com.hospital.his.medicaltech.application;

import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.medicaltech.domain.MedicalOrderState;
import com.hospital.his.medicaltech.domain.MedicalOrderType;
import com.hospital.his.medicaltech.persistence.mapper.MedicalOrderMapper;
import com.hospital.his.medicaltech.persistence.model.MedicalOrderRow;
import com.hospital.his.medicaltech.web.MedicalOrderResponse;
import com.hospital.his.medicaltech.web.MedicalOrderResultRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicalOrderService {
    private final MedicalOrderMapper mapper;

    public MedicalOrderService(MedicalOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResult<MedicalOrderResponse> search(
            MedicalOrderType type,
            MedicalOrderState state,
            String keyword,
            PageQuery page) {
        Long keywordId = parseKeywordId(keyword);
        if (hasKeyword(keyword) && keywordId == null) {
            return PageResult.of(List.of(), page, 0);
        }

        String stateName = state == null ? null : state.name();
        long total = countOrders(type, stateName, keywordId);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        List<MedicalOrderResponse> items = findOrders(type, stateName, keywordId, page)
                .stream()
                .map(row -> MedicalOrderResponse.from(type, row))
                .toList();
        return PageResult.of(items, page, total);
    }

    @Transactional
    public void accept(MedicalOrderType type, Long orderId, Long executorEmployeeId) {
        requireUpdated(switch (type) {
            case CHECK -> mapper.acceptCheck(orderId, executorEmployeeId);
            case INSPECTION -> mapper.acceptInspection(orderId, executorEmployeeId);
            case DISPOSAL -> mapper.acceptDisposal(orderId, executorEmployeeId);
        });
    }

    @Transactional
    public void execute(MedicalOrderType type, Long orderId) {
        requireUpdated(switch (type) {
            case CHECK -> mapper.executeCheck(orderId);
            case INSPECTION -> mapper.executeInspection(orderId);
            case DISPOSAL -> mapper.executeDisposal(orderId);
        });
    }

    @Transactional
    public void reportResult(MedicalOrderType type, Long orderId, MedicalOrderResultRequest request, Long resultEmployeeId) {
        requireUpdated(switch (type) {
            case CHECK -> mapper.reportCheckResult(orderId, resultEmployeeId, request.result(), request.remark());
            case INSPECTION -> mapper.reportInspectionResult(orderId, resultEmployeeId, request.result(), request.remark());
            case DISPOSAL -> mapper.reportDisposalResult(orderId, resultEmployeeId, request.result(), request.remark());
        });
    }

    private List<MedicalOrderRow> findOrders(MedicalOrderType type, String state, Long keywordId, PageQuery page) {
        return switch (type) {
            case CHECK -> mapper.findCheckOrders(state, keywordId, page.offset(), page.size());
            case INSPECTION -> mapper.findInspectionOrders(state, keywordId, page.offset(), page.size());
            case DISPOSAL -> mapper.findDisposalOrders(state, keywordId, page.offset(), page.size());
        };
    }

    private long countOrders(MedicalOrderType type, String state, Long keywordId) {
        return switch (type) {
            case CHECK -> mapper.countCheckOrders(state, keywordId);
            case INSPECTION -> mapper.countInspectionOrders(state, keywordId);
            case DISPOSAL -> mapper.countDisposalOrders(state, keywordId);
        };
    }

    private void requireUpdated(int affectedRows) {
        if (affectedRows != 1) {
            throw MedicalTechConflictException.invalidStateTransition();
        }
    }

    private boolean hasKeyword(String keyword) {
        return keyword != null && !keyword.isBlank();
    }

    private Long parseKeywordId(String keyword) {
        if (!hasKeyword(keyword)) {
            return null;
        }
        try {
            return Long.valueOf(keyword.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
