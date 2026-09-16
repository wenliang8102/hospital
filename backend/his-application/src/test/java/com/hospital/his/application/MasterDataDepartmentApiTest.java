package com.hospital.his.application;

import com.hospital.his.masterdata.persistence.mapper.DepartmentMapper;
import com.hospital.his.masterdata.persistence.model.DepartmentDraft;
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
class MasterDataDepartmentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentMapper departmentMapper;

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
    void searchesDepartmentsWithPagingAndFilters() throws Exception {
        insertDepartment("CARD", "心内科", "OUTPATIENT", true);
        insertDepartment("LAB", "检验科", "INSPECTION", true);
        insertDepartment("OLD_CARD", "旧心内科", "OUTPATIENT", false);

        mockMvc.perform(get("/api/master-data/departments")
                        .param("keyword", "心")
                        .param("type", "OUTPATIENT")
                        .param("active", "true")
                        .param("page", "1")
                        .param("size", "10")
                        .with(masterDataReadJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].code").value("CARD"))
                .andExpect(jsonPath("$.data.items[0].name").value("心内科"))
                .andExpect(jsonPath("$.data.items[0].type").value("OUTPATIENT"))
                .andExpect(jsonPath("$.data.items[0].active").value(true));
    }

    @Test
    void createsDepartmentAndReturnsSavedRow() throws Exception {
        mockMvc.perform(post("/api/master-data/departments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "PED",
                                  "name": "儿科",
                                  "type": "OUTPATIENT",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.code").value("PED"))
                .andExpect(jsonPath("$.data.name").value("儿科"))
                .andExpect(jsonPath("$.data.type").value("OUTPATIENT"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void updatesDepartmentAndReturnsSavedRow() throws Exception {
        Long id = insertDepartment("DERM", "皮肤科", "OUTPATIENT", true);

        mockMvc.perform(put("/api/master-data/departments/{id}", id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "DERM2",
                                  "name": "皮肤性病科",
                                  "type": "OUTPATIENT",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.code").value("DERM2"))
                .andExpect(jsonPath("$.data.name").value("皮肤性病科"));
    }

    @Test
    void changesDepartmentActiveState() throws Exception {
        Long id = insertDepartment("TEMP", "临时科室", "CHECK", true);

        mockMvc.perform(patch("/api/master-data/departments/{id}/active", id)
                        .contentType("application/json")
                        .content("{\"active\":false}")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void returnsConflictWhenDepartmentCodeAlreadyExists() throws Exception {
        insertDepartment("DUP", "重复科室", "OUTPATIENT", true);

        mockMvc.perform(post("/api/master-data/departments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "DUP",
                                  "name": "另一个科室",
                                  "type": "OUTPATIENT",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingDepartment() throws Exception {
        mockMvc.perform(put("/api/master-data/departments/{id}", 999L)
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "MISS",
                                  "name": "不存在科室",
                                  "type": "OUTPATIENT",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidDepartmentPayload() throws Exception {
        mockMvc.perform(post("/api/master-data/departments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "",
                                  "name": "",
                                  "type": "",
                                  "active": true
                                }
                                """)
                        .with(masterDataWriteJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsDepartmentWriteWhenPermissionMissing() throws Exception {
        mockMvc.perform(post("/api/master-data/departments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "code": "NOAUTH",
                                  "name": "无权限科室",
                                  "type": "OUTPATIENT",
                                  "active": true
                                }
                                """)
                        .with(masterDataReadJwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    private Long insertDepartment(String code, String name, String type, boolean active) {
        DepartmentDraft draft = new DepartmentDraft(code, name, type, active);
        departmentMapper.insert(draft);
        jdbcTemplate.update("UPDATE department SET active = ? WHERE id = ?", active, draft.getId());
        return draft.getId();
    }

    private RequestPostProcessor masterDataReadJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("master-data:read"));
    }

    private RequestPostProcessor masterDataWriteJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("master-data:write"));
    }
}
