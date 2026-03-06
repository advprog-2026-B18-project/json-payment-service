package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.WalletMinimalResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.UserTopUpTransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.JwtAuthenticationFilter;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.SecurityConfig;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.TransactionService;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.WalletService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    private PrivateKey testPrivateKey;

    private static final String TEST_USER_ID = "user-abc-123";
    private static final String TEST_EMAIL = "user@example.com";
    private static final String TEST_ROLE = "CUSTOMER";

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        testPrivateKey = keyPair.getPrivate();
    }

    private String generateTestToken(String userId, String email, String role, long expirationMillis) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(testPrivateKey)
                .compact();
    }

    @Test
    void getCurrentUserWalletReturns200() throws Exception {
        String token = generateTestToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLE, 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(TEST_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(TEST_ROLE);
        when(walletService.processGetCurrentUserWallet(TEST_USER_ID))
                .thenReturn(new WalletMinimalResponse("wallet-1", TEST_USER_ID, 10000L));

        mockMvc.perform(get("/wallet/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet_id").value("wallet-1"))
                .andExpect(jsonPath("$.user_id").value(TEST_USER_ID))
                .andExpect(jsonPath("$.balance").value(10000));
    }

    @Test
    void getCurrentUserTopUpTransactionsReturns200WithAllTransactions() throws Exception {
        String token = generateTestToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLE, 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(TEST_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(TEST_ROLE);
        when(transactionService.getCurrentUserTopUpTransactions(TEST_USER_ID)).thenReturn(List.of(
                new UserTopUpTransactionResponse("tx-1", 12000L, "PENDING", LocalDateTime.now()),
                new UserTopUpTransactionResponse("tx-2", 50000L, "SUCCESS", LocalDateTime.now())
        ));

        mockMvc.perform(get("/wallet/me/topup")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction_id").value("tx-1"))
                .andExpect(jsonPath("$[0].amount").value(12000))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].created_at").exists())
                .andExpect(jsonPath("$[1].transaction_id").value("tx-2"))
                .andExpect(jsonPath("$[1].amount").value(50000))
                .andExpect(jsonPath("$[1].status").value("SUCCESS"))
                .andExpect(jsonPath("$[1].created_at").exists());
    }

    @Test
    void createWalletReturns201() throws Exception {
        String userId = UUID.randomUUID().toString();
        String token = generateTestToken(TEST_USER_ID, TEST_EMAIL, TEST_ROLE, 3600000);

        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-123");
        wallet.setUserId(userId);
        wallet.setBalance(0L);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(TEST_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(TEST_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn(TEST_ROLE);
        when(walletService.createWalletForUser(userId)).thenReturn(wallet);

        mockMvc.perform(post("/wallet/{userId}", userId)
                .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.walletId").value("wallet-123"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.balance").value(0));
    }
}
