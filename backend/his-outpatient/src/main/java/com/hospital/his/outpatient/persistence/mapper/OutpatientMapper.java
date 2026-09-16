package com.hospital.his.outpatient.persistence.mapper;

import com.hospital.his.outpatient.persistence.model.PatientQueueRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface OutpatientMapper {
    Optional<PatientQueueRow> findPatient(@Param("registrationId") Long registrationId,
                                          @Param("employeeId") Long employeeId);

    List<PatientQueueRow> searchPatients(@Param("employeeId") Long employeeId,
                                         @Param("keyword") String keyword,
                                         @Param("state") String state,
                                         @Param("offset") long offset,
                                         @Param("limit") int limit);

    long countPatients(@Param("employeeId") Long employeeId,
                       @Param("keyword") String keyword,
                       @Param("state") String state);

    int transitionState(@Param("registrationId") Long registrationId,
                        @Param("employeeId") Long employeeId,
                        @Param("expectedState") String expectedState,
                        @Param("targetState") String targetState);
}
