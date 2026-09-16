package com.hospital.his.registration.application;

import com.hospital.his.registration.persistence.mapper.RegistrationMapper;
import com.hospital.his.registration.persistence.model.ChargeItemDraft;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ChargeItemService {
    private final RegistrationMapper mapper;

    public ChargeItemService(RegistrationMapper mapper) {
        this.mapper = mapper;
    }

    public Long create(Long registrationId, String itemType, Long sourceId, String itemName,
                       BigDecimal unitPrice, int quantity) {
        ChargeItemDraft draft = new ChargeItemDraft(registrationId, itemType, sourceId, itemName,
                unitPrice, quantity, unitPrice.multiply(BigDecimal.valueOf(quantity)));
        mapper.insertChargeItem(draft);
        return draft.getId();
    }
}
