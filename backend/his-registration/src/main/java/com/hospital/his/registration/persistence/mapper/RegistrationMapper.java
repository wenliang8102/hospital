package com.hospital.his.registration.persistence.mapper;

import com.hospital.his.registration.persistence.model.RegistrationDraft;
import com.hospital.his.registration.persistence.model.RegistrationReferenceRow;
import com.hospital.his.registration.persistence.model.RegistrationRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RegistrationMapper {
    Optional<RegistrationRow> findById(Long id);

    Optional<RegistrationRow> findByRequestNo(String requestNo);

    Optional<RegistrationReferenceRow> lockRegistrationReference(Long employeeId);

    boolean existsActiveSettlementCategory(Long settlementCategoryId);

    long countOccupiedSlot(
            @Param("employeeId") Long employeeId,
            @Param("visitStart") java.time.LocalDateTime visitStart,
            @Param("visitEnd") java.time.LocalDateTime visitEnd,
            @Param("noon") String noon);

    int insert(RegistrationDraft draft);

    int insertRegistrationCharge(
            @Param("registrationId") Long registrationId,
            @Param("itemName") String itemName,
            @Param("amount") java.math.BigDecimal amount);

    List<RegistrationRow> findByCaseNumber(@Param("caseNumber") String caseNumber, @Param("limit") int limit);

    List<RegistrationRow> search(
            @Param("keyword") String keyword,
            @Param("state") String state,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long count(@Param("keyword") String keyword, @Param("state") String state);

    long countPaidChargeItems(Long registrationId);

    int voidUnpaidChargeItems(Long registrationId);

    int transitionState(
            @Param("id") Long id,
            @Param("expectedState") String expectedState,
            @Param("targetState") String targetState);
}
