package com.hospital.his.masterdata.persistence.mapper;

import com.hospital.his.masterdata.persistence.model.DepartmentOption;
import com.hospital.his.masterdata.persistence.model.DiseaseOption;
import com.hospital.his.masterdata.persistence.model.EmployeeOption;
import com.hospital.his.masterdata.persistence.model.RegistrationLevelOption;
import com.hospital.his.masterdata.persistence.model.SettlementCategoryOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistrationLookupMapper {
    List<DepartmentOption> findDepartments(@Param("keyword") String keyword);

    List<EmployeeOption> findEmployees(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId);

    List<RegistrationLevelOption> findRegistrationLevels();

    List<SettlementCategoryOption> findSettlementCategories();

    List<DiseaseOption> findDiseases(@Param("keyword") String keyword, @Param("limit") int limit);
}
