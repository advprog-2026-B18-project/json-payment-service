package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.TransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.JwtAuthenticationFilter;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.SecurityConfig;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.TransactionService;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    private PrivateKey testPrivateKey;

    private static final String TEST_USER_ID = "user-abc-123";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_ROLE = "CUSTOMER";

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        testPrivateKey = keyPair.getPrivate();
    }

    private String generateTestToken(String userId, String username, String role, long expirationMillis) {
        return Jwts.builder()
                .claim("user_id", userId)
                .claim("username", username)
                .claim("role", role)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(testPrivateKey)
                .compact();
    }

    @Test
    void requestTopUpSuccessReturns201() throws Exception {
        String token = generateTestToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, 3600000);
        TransactionResponse response = new TransactionResponse(
                "tx-123",
                "TOPUP",
                15000L,
                "PENDING",
                LocalDateTime.now());

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(TEST_USERNAME);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(TEST_ROLE);
        when(transactionService.processRequestTopUp(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(TEST_USER_ID)))
                .thenReturn(response);

        mockMvc.perform(post("/transaction/topup")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "amount": 15000,
                          "payment_method": "VA",
                          "bank_code": "BCA",
                          "idempotency_key": "idem-123"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction_id").value("tx-123"))
                .andExpect(jsonPath("$.type").value("TOPUP"))
                .andExpect(jsonPath("$.amount").value(15000))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.created_at").exists());
    }

    @Test
    void requestTopUpAmountUnderMinimumReturns400() throws Exception {
        String token = generateTestToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(TEST_USERNAME);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(TEST_ROLE);
        when(transactionService.processRequestTopUp(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(TEST_USER_ID)))
                .thenThrow(new TransactionService.MinimumTopUpException());

        mockMvc.perform(post("/transaction/topup")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "amount": 9000,
                          "payment_method": "VA",
                          "bank_code": "BCA",
                          "idempotency_key": "idem-123"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_field").value("amount"))
                .andExpect(jsonPath("$.message").value("Minimum top-up is Rp 10.000"));
    }

    @Test
    void requestTopUpDuplicateIdempotencyKeyReturns409() throws Exception {
        String token = generateTestToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(TEST_USERNAME);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(TEST_ROLE);
        when(transactionService.processRequestTopUp(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(TEST_USER_ID)))
                .thenThrow(new TransactionService.DuplicateRequestException("existing-tx-id"));

        mockMvc.perform(post("/transaction/topup")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "amount": 15000,
                          "payment_method": "VA",
                          "bank_code": "BCA",
                          "idempotency_key": "idem-duplicate"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Duplicate request"))
                .andExpect(jsonPath("$.existing_transaction_id").value("existing-tx-id"));
    }
}
