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
class PlatformEmployeeAccountApiTest {

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
        when(authUserMapper.findPermissionsByUserId(1L)).thenReturn(List.of("platform:manage", "master-data:write"));
    }

    @Test
    void createsUpdatesAndDisablesEmployeeRecords() throws Exception {
        long outpatientId = insertDepartment("CARD", "心内科", "OUTPATIENT");
        long pharmacyId = insertDepartment("PHARM", "药房", "PHARMACY");
        long generalId = insertRegistLevel("GENERAL", "普通号");

        mockMvc.perform(post("/api/master-data/employees/manage")
                        .contentType("application/json")
                        .content("""
                                {
                                  "realName": "王医生",
                                  "departmentId": %d,
                                  "registLevelId": %d,
                                  "schedulingId": null,
                                  "active": true
                                }
                                """.formatted(outpatientId, generalId))
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.realName").value("王医生"))
                .andExpect(jsonPath("$.data.departmentName").value("心内科"))
                .andExpect(jsonPath("$.data.registLevelName").value("普通号"));

        Long employeeId = jdbcTemplate.queryForObject("SELECT id FROM employee WHERE real_name = ?", Long.class, "王医生");

        mockMvc.perform(put("/api/master-data/employees/manage/{id}", employeeId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "realName": "王药师",
                                  "departmentId": %d,
                                  "registLevelId": null,
                                  "schedulingId": null,
                                  "active": true
                                }
                                """.formatted(pharmacyId))
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("王药师"))
                .andExpect(jsonPath("$.data.departmentName").value("药房"));

        mockMvc.perform(patch("/api/master-data/employees/manage/{id}/active", employeeId)
                        .contentType("application/json")
                        .content("{\"active\":false}")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/master-data/employees/manage")
                        .param("active", "false")
                        .with(masterDataWriteJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].realName").value("王药师"));
    }

    @Test
    void createsUserAccountAndAssignsRoles() throws Exception {
        long departmentId = insertDepartment("CARD", "心内科", "OUTPATIENT");
        long employeeId = insertEmployee("李医生", departmentId);
        insertRole("OUTPATIENT_DOCTOR", "门诊医生");
        insertRole("PHARMACY_ADMIN", "药房管理员");

        mockMvc.perform(get("/api/platform/roles").with(platformManageJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("OUTPATIENT_DOCTOR"));

        mockMvc.perform(post("/api/platform/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "doctor-li",
                                  "password": "StrongPass123",
                                  "displayName": "李医生账号",
                                  "employeeId": %d,
                                  "enabled": true,
                                  "roleCodes": ["OUTPATIENT_DOCTOR"]
                                }
                                """.formatted(employeeId))
                        .with(platformManageJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("doctor-li"))
                .andExpect(jsonPath("$.data.employeeName").value("李医生"))
                .andExpect(jsonPath("$.data.roles[0].code").value("OUTPATIENT_DOCTOR"));

        Long userId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = ?", Long.class, "doctor-li");

        mockMvc.perform(put("/api/platform/users/{id}", userId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "displayName": "李医生",
                                  "employeeId": %d,
                                  "enabled": true,
                                  "roleCodes": ["OUTPATIENT_DOCTOR", "PHARMACY_ADMIN"]
                                }
                                """.formatted(employeeId))
                        .with(platformManageJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles.length()").value(2));

        mockMvc.perform(patch("/api/platform/users/{id}/enabled", userId)
                        .contentType("application/json")
                        .content("{\"enabled\":false}")
                        .with(platformManageJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(get("/api/platform/users")
                        .param("keyword", "doctor")
                        .with(platformManageJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].username").value("doctor-li"))
                .andExpect(jsonPath("$.data.items[0].roles.length()").value(2));

        mockMvc.perform(get("/api/platform/operation-logs")
                        .param("moduleCode", "platform")
                        .param("action", "USER_CREATE")
                        .param("keyword", "doctor-li")
                        .with(platformManageJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].operatorId").value(1))
                .andExpect(jsonPath("$.data.items[0].moduleCode").value("platform"))
                .andExpect(jsonPath("$.data.items[0].action").value("USER_CREATE"))
                .andExpect(jsonPath("$.data.items[0].targetType").value("USER"))
                .andExpect(jsonPath("$.data.items[0].targetId").value(userId.toString()))
                .andExpect(jsonPath("$.data.items[0].detail").value("创建账号 doctor-li"));

        mockMvc.perform(get("/api/platform/operation-logs")
                        .param("moduleCode", "platform")
                        .param("action", "USER_DISABLE")
                        .param("keyword", "doctor-li")
                        .with(platformManageJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].action").value("USER_DISABLE"))
                .andExpect(jsonPath("$.data.items[0].detail").value("停用账号 doctor-li"));
    }

    @Test
    void rejectsDuplicateUsername() throws Exception {
        insertRole("ROOT", "系统管理员");
        jdbcTemplate.update(
                "INSERT INTO sys_user (username, password_hash, display_name, enabled) VALUES (?, ?, ?, ?)",
                "admin2", "hash", "管理员", true);

        mockMvc.perform(post("/api/platform/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "admin2",
                                  "password": "StrongPass123",
                                  "displayName": "重复账号",
                                  "employeeId": null,
                                  "enabled": true,
                                  "roleCodes": ["ROOT"]
                                }
                                """)
                        .with(platformManageJwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void rejectsPlatformUserAccessWhenPermissionMissing() throws Exception {
        mockMvc.perform(get("/api/platform/users").with(masterDataWriteJwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    private long insertDepartment(String code, String name, String type) {
        jdbcTemplate.update("INSERT INTO department (dept_code, dept_name, dept_type, active) VALUES (?, ?, ?, TRUE)",
                code, name, type);
        return jdbcTemplate.queryForObject("SELECT id FROM department WHERE dept_code = ?", Long.class, code);
    }

    private long insertRegistLevel(String code, String name) {
        jdbcTemplate.update("INSERT INTO regist_level (regist_code, regist_name, active) VALUES (?, ?, TRUE)",
                code, name);
        return jdbcTemplate.queryForObject("SELECT id FROM regist_level WHERE regist_code = ?", Long.class, code);
    }

    private long insertEmployee(String realName, long departmentId) {
        jdbcTemplate.update("INSERT INTO employee (real_name, department_id, active) VALUES (?, ?, TRUE)",
                realName, departmentId);
        return jdbcTemplate.queryForObject("SELECT id FROM employee WHERE real_name = ?", Long.class, realName);
    }

    private void insertRole(String code, String name) {
        jdbcTemplate.update("INSERT INTO sys_role (code, name) VALUES (?, ?)", code, name);
    }

    private RequestPostProcessor masterDataWriteJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("master-data:write"));
    }

    private RequestPostProcessor platformManageJwt() {
        return jwt()
                .jwt(token -> token.subject("admin").claim("uid", 1L))
                .authorities(new SimpleGrantedAuthority("platform:manage"));
    }
}
