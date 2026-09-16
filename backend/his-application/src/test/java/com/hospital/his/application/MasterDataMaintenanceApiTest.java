package com.hospital.his.application;

import com.hospital.his.platform.security.AuthUserAccount;
import com.hospital.his.platform.security.AuthUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class MasterDataMaintenanceApiTest {

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
        when(authUserMapper.findPermissionsByUserId(1L)).thenReturn(List.of("master-data:manage"));
    }

    @Test
    void createsUpdatesAndDisablesDiseaseRecords() throws Exception {
        mockMvc.perform(post("/api/master-data/diseases/manage")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "D100",
                                  "name": "测试疾病",
                                  "icd": "T100",
                                  "category": "测试分类",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.code").value("D100"))
                .andExpect(jsonPath("$.data.name").value("测试疾病"))
                .andExpect(jsonPath("$.data.icd").value("T100"));

        Long id = jdbcTemplate.queryForObject("SELECT id FROM disease WHERE disease_code = ?", Long.class, "D100");

        mockMvc.perform(put("/api/master-data/diseases/manage/{id}", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "D101",
                                  "name": "测试疾病改",
                                  "icd": "T101",
                                  "category": "新分类",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.code").value("D101"))
                .andExpect(jsonPath("$.data.name").value("测试疾病改"));

        mockMvc.perform(patch("/api/master-data/diseases/manage/{id}/active", id)
                        .contentType("application/json")
                        .content("{\"active\":false}")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/master-data/diseases/manage")
                        .param("active", "false")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].code").value("D101"));
    }

    @Test
    void rejectsDuplicateDiseaseCode() throws Exception {
        insertDisease("DUP-D", "重复疾病");

        mockMvc.perform(post("/api/master-data/diseases/manage")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "DUP-D",
                                  "name": "另一个疾病",
                                  "icd": "DUP",
                                  "category": "测试",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void createsUpdatesAndDisablesDrugRecords() throws Exception {
        mockMvc.perform(post("/api/master-data/drugs/manage")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "DR100",
                                  "name": "测试药品",
                                  "format": "10mg*10片",
                                  "unit": "盒",
                                  "manufacturer": "测试药厂",
                                  "dosage": "口服",
                                  "type": "西药",
                                  "price": 9.80,
                                  "mnemonicCode": "CSYP",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.code").value("DR100"))
                .andExpect(jsonPath("$.data.price").value(9.80));

        Long id = jdbcTemplate.queryForObject("SELECT id FROM drug_info WHERE drug_code = ?", Long.class, "DR100");

        mockMvc.perform(put("/api/master-data/drugs/manage/{id}", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "DR101",
                                  "name": "测试药品改",
                                  "format": "20mg*10片",
                                  "unit": "盒",
                                  "manufacturer": "测试药厂",
                                  "dosage": "外用",
                                  "type": "外用药",
                                  "price": 12.30,
                                  "mnemonicCode": "CSYPG",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("DR101"))
                .andExpect(jsonPath("$.data.name").value("测试药品改"));

        mockMvc.perform(patch("/api/master-data/drugs/manage/{id}/active", id)
                        .contentType("application/json")
                        .content("{\"active\":false}")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/master-data/drugs/manage")
                        .param("active", "false")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].code").value("DR101"));
    }

    @Test
    void createsUpdatesAndDisablesMedicalTechnologyRecords() throws Exception {
        long labId = insertDepartment("LAB", "检验科", "INSPECTION");

        mockMvc.perform(post("/api/master-data/medical-technologies/manage")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "MT100",
                                  "name": "测试医技",
                                  "format": "样本",
                                  "price": 18.00,
                                  "type": "INSPECTION",
                                  "priceType": "检验费",
                                  "departmentId": %d,
                                  "active": true
                                }
                                """.formatted(labId))
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.code").value("MT100"))
                .andExpect(jsonPath("$.data.departmentName").value("检验科"));

        Long id = jdbcTemplate.queryForObject("SELECT id FROM medical_technology WHERE tech_code = ?", Long.class, "MT100");

        mockMvc.perform(put("/api/master-data/medical-technologies/manage/{id}", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "MT101",
                                  "name": "测试医技改",
                                  "format": "样本",
                                  "price": 22.00,
                                  "type": "INSPECTION",
                                  "priceType": "检验费",
                                  "departmentId": %d,
                                  "active": true
                                }
                                """.formatted(labId))
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("MT101"))
                .andExpect(jsonPath("$.data.name").value("测试医技改"));

        mockMvc.perform(patch("/api/master-data/medical-technologies/manage/{id}/active", id)
                        .contentType("application/json")
                        .content("{\"active\":false}")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/master-data/medical-technologies/manage")
                        .param("active", "false")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].code").value("MT101"));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingMaintainedRecord() throws Exception {
        mockMvc.perform(put("/api/master-data/drugs/manage/{id}", 999L)
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "MISS",
                                  "name": "不存在药品",
                                  "format": "1支",
                                  "unit": "支",
                                  "manufacturer": "测试药厂",
                                  "dosage": "注射",
                                  "type": "西药",
                                  "price": 1.00,
                                  "mnemonicCode": "MISS",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void createsUpdatesAndDisablesSchedulingRules() throws Exception {
        mockMvc.perform(post("/api/master-data/scheduling/manage")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "门诊白班",
                                  "weekRule": "00111111111000",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("门诊白班"))
                .andExpect(jsonPath("$.data.weekRule").value("00111111111000"))
                .andExpect(jsonPath("$.data.active").value(true));

        Long id = jdbcTemplate.queryForObject("SELECT id FROM scheduling WHERE rule_name = ?", Long.class, "门诊白班");

        mockMvc.perform(put("/api/master-data/scheduling/manage/{id}", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "门诊全天",
                                  "weekRule": "11111111111111",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("门诊全天"))
                .andExpect(jsonPath("$.data.weekRule").value("11111111111111"));

        mockMvc.perform(patch("/api/master-data/scheduling/manage/{id}/active", id)
                        .contentType("application/json")
                        .content("{\"active\":false}")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/master-data/scheduling/manage")
                        .param("keyword", "全天")
                        .param("active", "false")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("门诊全天"));
    }

    @Test
    void rejectsInvalidSchedulingWeekRule() throws Exception {
        mockMvc.perform(post("/api/master-data/scheduling/manage")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "错误规则",
                                  "weekRule": "111",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingSchedulingRule() throws Exception {
        mockMvc.perform(put("/api/master-data/scheduling/manage/{id}", 999L)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "不存在排班",
                                  "weekRule": "11111111111111",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private long insertDisease(String code, String name) {
        jdbcTemplate.update(
                "INSERT INTO disease (disease_code, disease_name, disease_icd, disease_category, active) VALUES (?, ?, ?, ?, TRUE)",
                code, name, "ICD", "测试");
        return jdbcTemplate.queryForObject("SELECT id FROM disease WHERE disease_code = ?", Long.class, code);
    }

    private long insertDepartment(String code, String name, String type) {
        jdbcTemplate.update("INSERT INTO department (dept_code, dept_name, dept_type, active) VALUES (?, ?, ?, TRUE)",
                code, name, type);
        return jdbcTemplate.queryForObject("SELECT id FROM department WHERE dept_code = ?", Long.class, code);
    }

    private RequestPostProcessor masterDataWriteJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("master-data:write"));
    }
}
