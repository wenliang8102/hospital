package com.hospital.his.registration.persistence.mapper;

import com.hospital.his.registration.persistence.model.RegistrationRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RegistrationMapper {
    Optional<RegistrationRow> findById(Long id);

    List<RegistrationRow> findByCaseNumber(@Param("caseNumber") String caseNumber, @Param("limit") int limit);

    int transitionState(
            @Param("id") Long id,
            @Param("expectedState") String expectedState,
            @Param("targetState") String targetState);
}
