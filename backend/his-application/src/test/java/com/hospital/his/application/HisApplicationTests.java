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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

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
    void medicalTechOrderRunsThroughExecutionStateMachine() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO check_request (
                    id, register_id, medical_technology_id, request_info, body_position, state
                ) VALUES (1001, 1, 2, '胸部检查', '胸部', 'PAID')
                """);

        mockMvc.perform(get("/api/medical-orders")
                        .param("type", "CHECK")
                        .param("state", "PAID")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[0].type").value("CHECK"))
                .andExpect(jsonPath("$.data.items[0].state").value("PAID"));

        mockMvc.perform(post("/api/medical-orders/CHECK/1001/accept")
                        .with(jwt().jwt(token -> token.claim("employeeId", 7L))))
                .andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(orderState("check_request", 1001L)).isEqualTo("ACCEPTED");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT executor_employee_id FROM check_request WHERE id = 1001", Long.class))
                .isEqualTo(7L);

        mockMvc.perform(post("/api/medical-orders/CHECK/1001/execute").with(jwt()))
                .andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(orderState("check_request", 1001L)).isEqualTo("EXECUTED");

        mockMvc.perform(post("/api/medical-orders/CHECK/1001/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"未见明显异常\",\"remark\":\"复查随诊\"}")
                        .with(jwt().jwt(token -> token.claim("employeeId", 8L))))
                .andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(orderState("check_request", 1001L)).isEqualTo("RESULT_REPORTED");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT result FROM check_request WHERE id = 1001", String.class))
                .isEqualTo("未见明显异常");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT result_employee_id FROM check_request WHERE id = 1001", Long.class))
                .isEqualTo(8L);

        mockMvc.perform(get("/api/medical-orders")
                        .param("type", "CHECK")
                        .param("state", "RESULT_REPORTED")
                        .param("keyword", "1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[0].registrationId").value(1))
                .andExpect(jsonPath("$.data.items[0].executorEmployeeId").value(7))
                .andExpect(jsonPath("$.data.items[0].resultEmployeeId").value(8))
                .andExpect(jsonPath("$.data.items[0].result").value("未见明显异常"))
                .andExpect(jsonPath("$.data.items[0].remark").value("复查随诊"));

        mockMvc.perform(get("/api/medical-orders")
                        .param("type", "CHECK")
                        .param("keyword", "1001")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].state").value("RESULT_REPORTED"));

        mockMvc.perform(get("/api/medical-orders")
                        .param("type", "CHECK")
                        .param("keyword", "非数字关键字")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(post("/api/medical-orders/CHECK/1001/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"重复录入\"}")
                        .with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void medicalTechRejectsInvalidExecutionTransitions() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO check_request (
                    id, register_id, medical_technology_id, request_info, body_position, state
                ) VALUES (1002, 1, 2, '腹部检查', '腹部', 'PAID')
                """);

        mockMvc.perform(post("/api/medical-orders/CHECK/1002/execute").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
        org.assertj.core.api.Assertions.assertThat(orderState("check_request", 1002L)).isEqualTo("PAID");

        mockMvc.perform(post("/api/medical-orders/CHECK/1002/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"不应直接出结果\"}")
                        .with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
        org.assertj.core.api.Assertions.assertThat(orderState("check_request", 1002L)).isEqualTo("PAID");

        mockMvc.perform(post("/api/medical-orders/CHECK/9999/accept").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    private String orderState(String tableName, Long id) {
        String sql = switch (tableName) {
            case "check_request" -> "SELECT state FROM check_request WHERE id = ?";
            case "inspection_request" -> "SELECT state FROM inspection_request WHERE id = ?";
            case "disposal_request" -> "SELECT state FROM disposal_request WHERE id = ?";
            default -> throw new IllegalArgumentException("Unsupported medical order table");
        };
        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    @Test
    void pharmacyDispensesPrescriptionAndWritesStockTransaction() throws Exception {
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (10, 5, 0)");
        jdbcTemplate.update("""
                INSERT INTO prescription (
                    id, register_id, drug_id, drug_usage, drug_number, state
                ) VALUES (2001, 1, 10, '口服，每日两次', 3, 'PAID')
                """);

        mockMvc.perform(get("/api/pharmacy/prescriptions")
                        .param("state", "PAID")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(2001))
                .andExpect(jsonPath("$.data.items[0].state").value("PAID"))
                .andExpect(jsonPath("$.data.items[0].stockQuantity").value(5));

        mockMvc.perform(post("/api/pharmacy/prescriptions/2001/dispense")
                        .with(jwt().jwt(token -> token.claim("uid", 1L).claim("employeeId", 7L))))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(prescriptionState(2001L)).isEqualTo("DISPENSED");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT quantity FROM drug_stock WHERE drug_id = 10", Integer.class))
                .isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT version FROM drug_stock WHERE drug_id = 10", Long.class))
                .isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT dispensed_by FROM prescription WHERE id = 2001", Long.class))
                .isEqualTo(7L);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT transaction_type FROM drug_stock_transaction WHERE prescription_id = 2001",
                                String.class))
                .isEqualTo("DISPENSE");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT quantity_after FROM drug_stock_transaction WHERE prescription_id = 2001",
                                Integer.class))
                .isEqualTo(2);

        mockMvc.perform(get("/api/pharmacy/stock-transactions")
                        .param("drugId", "10")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].drugId").value(10))
                .andExpect(jsonPath("$.data.items[0].prescriptionId").value(2001))
                .andExpect(jsonPath("$.data.items[0].transactionType").value("DISPENSE"))
                .andExpect(jsonPath("$.data.items[0].quantityBefore").value(5))
                .andExpect(jsonPath("$.data.items[0].quantityAfter").value(2));

        mockMvc.perform(post("/api/pharmacy/prescriptions/2001/dispense")
                        .with(jwt().jwt(token -> token.claim("uid", 1L).claim("employeeId", 7L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void pharmacyRejectsDispenseWhenStockIsInsufficient() throws Exception {
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (11, 2, 0)");
        jdbcTemplate.update("""
                INSERT INTO prescription (
                    id, register_id, drug_id, drug_usage, drug_number, state
                ) VALUES (2002, 1, 11, '口服，每日一次', 9, 'PAID')
                """);

        mockMvc.perform(post("/api/pharmacy/prescriptions/2002/dispense")
                        .with(jwt().jwt(token -> token.claim("uid", 1L).claim("employeeId", 7L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        org.assertj.core.api.Assertions.assertThat(prescriptionState(2002L)).isEqualTo("PAID");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT quantity FROM drug_stock WHERE drug_id = 11", Integer.class))
                .isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM drug_stock_transaction WHERE prescription_id = 2002", Long.class))
                .isZero();
    }

    @Test
    void pharmacyReturnsPrescriptionAndRestoresStock() throws Exception {
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (12, 4, 1)");
        jdbcTemplate.update("""
                INSERT INTO prescription (
                    id, register_id, drug_id, drug_usage, drug_number, state, dispensed_by
                ) VALUES (2003, 1, 12, '外用，每日一次', 2, 'DISPENSED', 7)
                """);

        mockMvc.perform(post("/api/pharmacy/prescriptions/2003/return")
                        .with(jwt().jwt(token -> token.claim("uid", 1L).claim("employeeId", 7L))))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(prescriptionState(2003L)).isEqualTo("RETURNED");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT quantity FROM drug_stock WHERE drug_id = 12", Integer.class))
                .isEqualTo(6);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT version FROM drug_stock WHERE drug_id = 12", Long.class))
                .isEqualTo(2L);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT transaction_type FROM drug_stock_transaction WHERE prescription_id = 2003",
                                String.class))
                .isEqualTo("RETURN");
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT quantity_after FROM drug_stock_transaction WHERE prescription_id = 2003",
                                Integer.class))
                .isEqualTo(6);

        mockMvc.perform(get("/api/pharmacy/stock-transactions")
                        .param("prescriptionId", "2003")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].drugId").value(12))
                .andExpect(jsonPath("$.data.items[0].prescriptionId").value(2003))
                .andExpect(jsonPath("$.data.items[0].transactionType").value("RETURN"))
                .andExpect(jsonPath("$.data.items[0].quantityBefore").value(4))
                .andExpect(jsonPath("$.data.items[0].quantityAfter").value(6));

        mockMvc.perform(post("/api/pharmacy/prescriptions/2003/return")
                        .with(jwt().jwt(token -> token.claim("uid", 1L).claim("employeeId", 7L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void pharmacyListsLowStocksAndSearchesByDrugId() throws Exception {
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (30, 0, 2)");
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (31, 5, 1)");
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (32, 25, 0)");

        mockMvc.perform(get("/api/pharmacy/stocks")
                        .param("maxQuantity", "5")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items[0].drugId").value(30))
                .andExpect(jsonPath("$.data.items[0].quantity").value(0))
                .andExpect(jsonPath("$.data.items[1].drugId").value(31))
                .andExpect(jsonPath("$.data.items[1].quantity").value(5));

        mockMvc.perform(get("/api/pharmacy/stocks")
                        .param("keyword", "32")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].drugId").value(32))
                .andExpect(jsonPath("$.data.items[0].quantity").value(25));

        mockMvc.perform(get("/api/pharmacy/stocks")
                        .param("keyword", "ABC")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void pharmacyInboundsAndAdjustsStockWithTransactions() throws Exception {
        jdbcTemplate.update("INSERT INTO drug_stock (drug_id, quantity, version) VALUES (40, 2, 0)");

        mockMvc.perform(post("/api/pharmacy/stocks/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"drugId\":40,\"quantity\":5}")
                        .with(jwt().jwt(token -> token.claim("uid", 1L))))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT quantity FROM drug_stock WHERE drug_id = 40", Integer.class))
                .isEqualTo(7);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT version FROM drug_stock WHERE drug_id = 40", Long.class))
                .isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject(
                                "SELECT transaction_type FROM drug_stock_transaction WHERE drug_id = 40",
                                String.class))
                .isEqualTo("INBOUND");

        mockMvc.perform(post("/api/pharmacy/stocks/adjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"drugId\":40,\"targetQuantity\":3}")
                        .with(jwt().jwt(token -> token.claim("uid", 1L))))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT quantity FROM drug_stock WHERE drug_id = 40", Integer.class))
                .isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(
                        jdbcTemplate.queryForObject("SELECT version FROM drug_stock WHERE drug_id = 40", Long.class))
                .isEqualTo(2L);

        mockMvc.perform(get("/api/pharmacy/stock-transactions")
                        .param("drugId", "40")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items[0].transactionType").value("ADJUST_DECREASE"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(4))
                .andExpect(jsonPath("$.data.items[0].quantityBefore").value(7))
                .andExpect(jsonPath("$.data.items[0].quantityAfter").value(3))
                .andExpect(jsonPath("$.data.items[1].transactionType").value("INBOUND"))
                .andExpect(jsonPath("$.data.items[1].quantity").value(5))
                .andExpect(jsonPath("$.data.items[1].quantityBefore").value(2))
                .andExpect(jsonPath("$.data.items[1].quantityAfter").value(7));
    }

    @Test
    void pharmacyRejectsInvalidStockOperations() throws Exception {
        mockMvc.perform(post("/api/pharmacy/stocks/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"drugId\":41,\"quantity\":0}")
                        .with(jwt().jwt(token -> token.claim("uid", 1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/pharmacy/stocks/adjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"drugId\":41,\"targetQuantity\":-1}")
                        .with(jwt().jwt(token -> token.claim("uid", 1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/pharmacy/stocks/adjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"drugId\":41,\"targetQuantity\":5}")
                        .with(jwt().jwt(token -> token.claim("uid", 1L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STOCK_OPERATION"));
    }

    private String prescriptionState(Long id) {
        return jdbcTemplate.queryForObject("SELECT state FROM prescription WHERE id = ?", String.class, id);
    }
}
