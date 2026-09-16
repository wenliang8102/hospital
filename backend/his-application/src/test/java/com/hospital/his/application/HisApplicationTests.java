package com.hospital.his.application;

import com.hospital.his.platform.security.AuthUserAccount;
import com.hospital.his.platform.security.AuthUserMapper;
import com.hospital.his.masterdata.persistence.mapper.DepartmentMapper;
import com.hospital.his.masterdata.persistence.model.DepartmentDraft;
import com.hospital.his.masterdata.persistence.DepartmentRepository;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.pharmacy.persistence.mapper.DrugStockMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class HisApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DrugStockMapper drugStockMapper;

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
                        .content("{\"username\":\"a\",\"password\":\"1234\"}"))
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

    @Test
    void departmentMapperExecutesCrudAndPagingSql() {
        DepartmentDraft draft = new DepartmentDraft("CARD", "心内科", "CLINICAL", true);
        departmentMapper.insert(draft);

        org.assertj.core.api.Assertions.assertThat(draft.getId()).isNotNull();
        var saved = departmentMapper.findById(draft.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(saved.code()).isEqualTo("CARD");
        org.assertj.core.api.Assertions.assertThat(saved.name()).isEqualTo("心内科");
        org.assertj.core.api.Assertions.assertThat(departmentMapper.count("心内", "CLINICAL", true)).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(departmentMapper.findPage(null, null, true, 0, 20))
                .hasSize(1);
        var page = departmentRepository.search("心内", "CLINICAL", true, PageQuery.of(1, 20));
        org.assertj.core.api.Assertions.assertThat(page.total()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(page.items()).containsExactly(saved);
    }

    @Test
    void drugStockMapperUsesVersionAndPreventsNegativeStock() {
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (1, 10, 0)");

        org.assertj.core.api.Assertions.assertThat(drugStockMapper.changeQuantity(1L, -3, 0)).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(drugStockMapper.changeQuantity(1L, -8, 1)).isZero();
        org.assertj.core.api.Assertions.assertThat(drugStockMapper.changeQuantity(1L, 1, 0)).isZero();
        var stock = drugStockMapper.findByDrugId(1L).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(stock.quantity()).isEqualTo(7);
        org.assertj.core.api.Assertions.assertThat(stock.version()).isEqualTo(1L);
    }

    @Test
    void completesRegistrationSearchAndCancellationFlow() throws Exception {
        seedRegistrationReferences();
        String visitDate = LocalDate.now().plusDays(1) + "T09:00:00";
        String body = registrationRequest("request-001", 1, visitDate);

        mockMvc.perform(post("/api/registration/case-numbers").with(registrationJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.matchesPattern("H\\d{8}[A-Z0-9]{8}")));

        mockMvc.perform(get("/api/master-data/departments").with(registrationJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("内科"));

        String response = mockMvc.perform(post("/api/registrations")
                        .with(registrationJwt())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("REGISTERED"))
                .andExpect(jsonPath("$.data.registrationFee").value(8.00))
                .andReturn().getResponse().getContentAsString();
        long registrationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).path("data").path("id").asLong();

        mockMvc.perform(post("/api/registrations")
                        .with(registrationJwt())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(registrationId));
        org.assertj.core.api.Assertions.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `register`", Long.class)).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM charge_item", Long.class)).isEqualTo(1);

        mockMvc.perform(get("/api/registrations")
                        .param("keyword", "张三")
                        .with(registrationJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].employeeName").value("李医生"));

        mockMvc.perform(post("/api/registrations/{id}/cancel", registrationId).with(registrationJwt()))
                .andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT visit_state FROM `register` WHERE id = ?", String.class, registrationId))
                .isEqualTo("CANCELLED");
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT state FROM charge_item WHERE register_id = ?", String.class, registrationId))
                .isEqualTo("VOID");

        mockMvc.perform(post("/api/registrations/{id}/cancel", registrationId).with(registrationJwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void rejectsDoctorFromAnotherDepartment() throws Exception {
        seedRegistrationReferences();
        jdbcTemplate.update("INSERT INTO department (id, dept_code, dept_name, dept_type) VALUES (2, 'SURG', '外科', 'OUTPATIENT')");

        mockMvc.perform(post("/api/registrations")
                        .with(registrationJwt())
                        .contentType("application/json")
                        .content(registrationRequest("request-002", 2, LocalDate.now().plusDays(1) + "T09:00:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsInvalidRegistrationPageParameters() throws Exception {
        mockMvc.perform(get("/api/registrations")
                        .param("page", "0")
                        .with(registrationJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void completesOutpatientConsultationWithMedicalRecordAndDiagnosis() throws Exception {
        seedRegistrationReferences();
        jdbcTemplate.update("INSERT INTO disease (id, disease_code, disease_name, disease_icd) VALUES (1, 'J00', '急性鼻咽炎', 'J00')");
        String registrationResponse = mockMvc.perform(post("/api/registrations")
                        .with(registrationJwt())
                        .contentType("application/json")
                        .content(registrationRequest("outpatient-001", 1,
                                LocalDate.now() + "T09:00:00")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long registrationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(registrationResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/outpatient/patients").with(outpatientJwt(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].realName").value("张三"));

        mockMvc.perform(post("/api/registrations/{id}/accept", registrationId).with(outpatientJwt(2)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/registrations/{id}/accept", registrationId).with(outpatientJwt(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("IN_CONSULTATION"));

        mockMvc.perform(post("/api/registrations/{id}/complete", registrationId).with(outpatientJwt(1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));

        String medicalRecord = """
                {
                  "chiefComplaint": "鼻塞、流涕一天",
                  "presentIllness": "无发热",
                  "diagnosis": "急性鼻咽炎",
                  "treatmentPlan": "对症治疗",
                  "diseaseIds": [1]
                }
                """;
        mockMvc.perform(put("/api/registrations/{id}/medical-record", registrationId)
                        .with(outpatientJwt(1))
                        .contentType("application/json")
                        .content(medicalRecord))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chiefComplaint").value("鼻塞、流涕一天"))
                .andExpect(jsonPath("$.data.diseases[0].code").value("J00"));
        mockMvc.perform(put("/api/registrations/{id}/medical-record", registrationId)
                        .with(outpatientJwt(1))
                        .contentType("application/json")
                        .content(medicalRecord))
                .andExpect(status().isOk());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM medical_record WHERE register_id = ?", Long.class, registrationId))
                .isEqualTo(1);

        mockMvc.perform(get("/api/master-data/diseases")
                        .param("keyword", "J00")
                        .with(outpatientJwt(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("急性鼻咽炎"));
        mockMvc.perform(post("/api/registrations/{id}/complete", registrationId).with(outpatientJwt(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("COMPLETED"));
    }

    @Test
    void outpatientActionsRequireEmployeeBinding() throws Exception {
        mockMvc.perform(get("/api/outpatient/patients")
                        .with(jwt().authorities(new SimpleGrantedAuthority("outpatient:write"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void createsClinicalOrdersAndCoordinatesPaymentAndPartialRefund() throws Exception {
        seedRegistrationReferences();
        jdbcTemplate.update("""
                INSERT INTO medical_technology
                    (id, tech_code, tech_name, tech_price, tech_type, department_id)
                VALUES (1, 'XRAY', '胸部正位片', 120.00, 'CHECK', 1),
                       (2, 'DRESSING', '换药', 30.00, 'DISPOSAL', 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO drug_info
                    (id, drug_code, drug_name, drug_format, drug_unit, drug_price, mnemonic_code)
                VALUES (1, 'AMOX', '阿莫西林胶囊', '0.25g*24粒', '盒', 10.00, 'AMXL')
                """);

        String registrationResponse = mockMvc.perform(post("/api/registrations")
                        .with(registrationJwt())
                        .contentType("application/json")
                        .content(registrationRequest("billing-001", 1, LocalDate.now() + "T09:00:00")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long registrationId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(registrationResponse).path("data").path("id").asLong();
        mockMvc.perform(post("/api/registrations/{id}/accept", registrationId).with(outpatientJwt(1)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/master-data/medical-technologies")
                        .param("type", "CHECK").with(outpatientJwt(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("胸部正位片"));
        mockMvc.perform(get("/api/master-data/drugs").param("keyword", "AMXL").with(outpatientJwt(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("阿莫西林胶囊"));

        mockMvc.perform(post("/api/registrations/{id}/medical-orders", registrationId)
                        .with(outpatientJwt(1))
                        .contentType("application/json")
                        .content("""
                                [{"type":"CHECK","medicalTechnologyId":1,
                                  "requestInfo":"咳嗽三天","bodyPosition":"胸部"}]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].state").value("CREATED"));
        mockMvc.perform(post("/api/registrations/{id}/prescriptions", registrationId)
                        .with(outpatientJwt(1))
                        .contentType("application/json")
                        .content("""
                                [{"drugId":1,"drugUsage":"口服，每日三次","drugNumber":2}]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].totalAmount").value(20.00));

        mockMvc.perform(get("/api/registrations/{id}/charge-items", registrationId).with(billingJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
        long checkChargeId = jdbcTemplate.queryForObject(
                "SELECT id FROM charge_item WHERE register_id = ? AND item_type = 'CHECK'",
                Long.class, registrationId);
        long prescriptionChargeId = jdbcTemplate.queryForObject(
                "SELECT id FROM charge_item WHERE register_id = ? AND item_type = 'PRESCRIPTION'",
                Long.class, registrationId);

        String paymentBody = """
                {"registrationId":%d,"chargeItemIds":[%d,%d],
                 "paymentMethod":"WECHAT","idempotencyKey":"payment-001"}
                """.formatted(registrationId, checkChargeId, prescriptionChargeId);
        String paymentResponse = mockMvc.perform(post("/api/payments")
                        .with(billingJwt()).contentType("application/json").content(paymentBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(140.00))
                .andReturn().getResponse().getContentAsString();
        long paymentId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(paymentResponse).path("data").path("id").asLong();
        mockMvc.perform(post("/api/payments")
                        .with(billingJwt()).contentType("application/json").content(paymentBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(paymentId));
        String conflictingPaymentBody = """
                {"registrationId":%d,"chargeItemIds":[%d],
                 "paymentMethod":"WECHAT","idempotencyKey":"payment-001"}
                """.formatted(registrationId, checkChargeId);
        mockMvc.perform(post("/api/payments")
                        .with(billingJwt()).contentType("application/json").content(conflictingPaymentBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT state FROM check_request WHERE register_id = ?", String.class, registrationId))
                .isEqualTo("PAID");
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT state FROM prescription WHERE register_id = ?", String.class, registrationId))
                .isEqualTo("PAID");

        String refundBody = """
                {"originalTransactionId":%d,"chargeItemIds":[%d],
                 "reason":"患者要求退药","idempotencyKey":"refund-001"}
                """.formatted(paymentId, prescriptionChargeId);
        String refundResponse = mockMvc.perform(post("/api/refunds")
                        .with(billingJwt()).contentType("application/json").content(refundBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(20.00))
                .andExpect(jsonPath("$.data.reason").value("患者要求退药"))
                .andReturn().getResponse().getContentAsString();
        long refundId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(refundResponse).path("data").path("id").asLong();
        mockMvc.perform(post("/api/refunds")
                        .with(billingJwt()).contentType("application/json").content(refundBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(refundId));
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT state FROM prescription WHERE register_id = ?", String.class, registrationId))
                .isEqualTo("REFUNDED");
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment_transaction", Long.class)).isEqualTo(2);
    }

    private JwtRequestPostProcessor registrationJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("registration:write"));
    }

    private JwtRequestPostProcessor outpatientJwt(long employeeId) {
        return jwt()
                .jwt(builder -> builder.claim("employeeId", employeeId))
                .authorities(new SimpleGrantedAuthority("outpatient:write"));
    }

    private JwtRequestPostProcessor billingJwt() {
        return jwt()
                .jwt(builder -> builder.claim("uid", 1L))
                .authorities(new SimpleGrantedAuthority("registration:write"));
    }

    private void seedRegistrationReferences() {
        jdbcTemplate.update("INSERT INTO department (id, dept_code, dept_name, dept_type) VALUES (1, 'INTERNAL', '内科', 'OUTPATIENT')");
        jdbcTemplate.update("INSERT INTO regist_level (id, regist_code, regist_name, regist_fee, regist_quota) VALUES (1, 'GENERAL', '普通号', 8.00, 10)");
        jdbcTemplate.update("INSERT INTO settle_category (id, settle_code, settle_name) VALUES (1, 'SELF_PAY', '自费')");
        jdbcTemplate.update("INSERT INTO employee (id, real_name, department_id, regist_level_id) VALUES (1, '李医生', 1, 1)");
    }

    private String registrationRequest(String requestId, long departmentId, String visitDate) {
        return """
                {
                  "requestId": "%s",
                  "caseNumber": "H202609160001",
                  "realName": "张三",
                  "gender": "MALE",
                  "age": 36,
                  "ageType": "YEAR",
                  "visitDate": "%s",
                  "noon": "AM",
                  "departmentId": %d,
                  "employeeId": 1,
                  "registrationLevelId": 1,
                  "settlementCategoryId": 1,
                  "booked": false,
                  "registrationMethod": "CASH"
                }
                """.formatted(requestId, visitDate, departmentId);
    }
}
