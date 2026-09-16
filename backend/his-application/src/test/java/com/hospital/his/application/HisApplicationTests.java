package com.hospital.his.application;

import com.hospital.his.platform.security.AuthUserAccount;
import com.hospital.his.platform.security.AuthUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class HisApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthUserMapper authUserMapper;

    @BeforeEach
    void setUpUser() {
        AuthUserAccount account = new AuthUserAccount(
                1L, "admin", passwordEncoder.encode("admin-password"), "系统管理员", null, true);
        when(authUserMapper.findByUsername("admin")).thenReturn(Optional.of(account));
        when(authUserMapper.findRolesByUserId(1L)).thenReturn(List.of("ROOT"));
        when(authUserMapper.findPermissionsByUserId(1L)).thenReturn(List.of("platform:manage"));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void rejectsUnauthenticatedModuleRequest() throws Exception {
        mockMvc.perform(get("/api/registration/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_TOKEN_INVALID"));
    }

    @Test
    void exposesModuleStatusEndpointToAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/registration/status").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.code").value("registration"))
                .andExpect(jsonPath("$.data.status").value("READY"));
    }

    @Test
    void validatesLoginRequestBeforeAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"a\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void authenticatesUserAndIssuesJwt() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"admin-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("ROOT"));
    }
}
