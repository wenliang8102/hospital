package com.hospital.his.application;

import com.hospital.his.platform.security.AuthUserAccount;
import com.hospital.his.platform.security.AuthUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class MasterDataLookupApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private AuthUserMapper authUserMapper;

    @BeforeEach
    void setUpUser() {
        AuthUserAccount account = new AuthUserAccount(
                1L, "admin", passwordEncoder.encode("admin-password"), "系统管理员", null, true);
        when(authUserMapper.findByUsername("admin")).thenReturn(Optional.of(account));
        when(authUserMapper.findRolesByUserId(1L)).thenReturn(List.of("ROOT"));
        when(authUserMapper.findPermissionsByUserId(1L)).thenReturn(List.of("master-data:read"));
    }

    @Test
    void listsActiveEmployeesByDepartmentAndKeyword() throws Exception {
        long outpatientId = insertDepartment("CARD", "心内科", "OUTPATIENT", true);
        long pharmacyId = insertDepartment("PHARM", "药房", "PHARMACY", true);
        long registLevelId = insertRegistLevel("GENERAL", "普通号", true);
        jdbcTemplate.update("INSERT INTO employee (real_name, department_id, regist_level_id, active) VALUES (?, ?, ?, ?)",
                "王医生", outpatientId, registLevelId, true);
        jdbcTemplate.update("INSERT INTO employee (real_name, department_id, regist_level_id, active) VALUES (?, ?, ?, ?)",
                "王药师", pharmacyId, registLevelId, true);
        jdbcTemplate.update("INSERT INTO employee (real_name, department_id, regist_level_id, active) VALUES (?, ?, ?, ?)",
                "王停用", outpatientId, registLevelId, false);

        mockMvc.perform(get("/api/master-data/employees")
                        .param("keyword", "王")
                        .param("departmentId", Long.toString(outpatientId))
                        .with(masterDataReadJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].realName").value("王医生"))
                .andExpect(jsonPath("$.data[0].departmentId").value(outpatientId))
                .andExpect(jsonPath("$.data[0].departmentName").value("心内科"))
                .andExpect(jsonPath("$.data[0].registLevelName").value("普通号"));
    }

    @Test
    void pagesDiseasesByKeyword() throws Exception {
        jdbcTemplate.update("INSERT INTO disease (disease_code, disease_name, disease_icd, disease_category, active) VALUES (?, ?, ?, ?, ?)",
                "D001", "普通感冒", "J00", "呼吸系统", true);
        jdbcTemplate.update("INSERT INTO disease (disease_code, disease_name, disease_icd, disease_category, active) VALUES (?, ?, ?, ?, ?)",
                "D002", "高血压", "I10", "循环系统", true);

        mockMvc.perform(get("/api/master-data/diseases")
                        .param("keyword", "感")
                        .param("page", "1")
                        .param("size", "10")
                        .with(masterDataReadJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].code").value("D001"))
                .andExpect(jsonPath("$.data.items[0].name").value("普通感冒"))
                .andExpect(jsonPath("$.data.items[0].icd").value("J00"));
    }

    @Test
    void pagesDrugsByNameCodeOrMnemonic() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO drug_info
                    (drug_code, drug_name, drug_format, drug_unit, manufacturer, drug_dosage, drug_type, drug_price, mnemonic_code, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "DRUG001", "阿莫西林胶囊", "0.25g*24粒", "盒", "示例药厂", "口服", "抗生素", 12.50, "AMXL", true);

        mockMvc.perform(get("/api/master-data/drugs")
                        .param("keyword", "AMXL")
                        .param("page", "1")
                        .param("size", "10")
                        .with(masterDataReadJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].code").value("DRUG001"))
                .andExpect(jsonPath("$.data.items[0].name").value("阿莫西林胶囊"))
                .andExpect(jsonPath("$.data.items[0].price").value(12.50));
    }

    @Test
    void pagesMedicalTechnologiesWithDepartmentName() throws Exception {
        long inspectionId = insertDepartment("LAB", "检验科", "INSPECTION", true);
        jdbcTemplate.update("""
                INSERT INTO medical_technology
                    (tech_code, tech_name, tech_format, tech_price, tech_type, price_type, department_id, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, "MT001", "血常规", "全血", 18.00, "INSPECTION", "检验费", inspectionId, true);

        mockMvc.perform(get("/api/master-data/medical-technologies")
                        .param("keyword", "血")
                        .param("page", "1")
                        .param("size", "10")
                        .with(masterDataReadJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].code").value("MT001"))
                .andExpect(jsonPath("$.data.items[0].name").value("血常规"))
                .andExpect(jsonPath("$.data.items[0].departmentName").value("检验科"));
    }

    @Test
    void rejectsUnsupportedLookupResource() throws Exception {
        mockMvc.perform(get("/api/master-data/unknown").with(masterDataReadJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void rejectsLookupWhenPermissionMissing() throws Exception {
        mockMvc.perform(get("/api/master-data/diseases")
                        .with(jwt().authorities(new SimpleGrantedAuthority("platform:manage"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    private long insertDepartment(String code, String name, String type, boolean active) {
        jdbcTemplate.update("INSERT INTO department (dept_code, dept_name, dept_type, active) VALUES (?, ?, ?, ?)",
                code, name, type, active);
        return jdbcTemplate.queryForObject("SELECT id FROM department WHERE dept_code = ?", Long.class, code);
    }

    private long insertRegistLevel(String code, String name, boolean active) {
        jdbcTemplate.update("INSERT INTO regist_level (regist_code, regist_name, active) VALUES (?, ?, ?)",
                code, name, active);
        return jdbcTemplate.queryForObject("SELECT id FROM regist_level WHERE regist_code = ?", Long.class, code);
    }

    private RequestPostProcessor masterDataReadJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("master-data:read"));
    }
}
