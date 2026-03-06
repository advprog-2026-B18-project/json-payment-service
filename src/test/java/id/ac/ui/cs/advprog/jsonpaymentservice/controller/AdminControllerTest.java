package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ConfirmTopUpResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.PendingTransactionResponse;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    private PrivateKey testPrivateKey;

    private static final String ADMIN_USER_ID = "admin-1";
    private static final String ADMIN_EMAIL = "admin@example.com";

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        testPrivateKey = keyPair.getPrivate();
    }

    private String generateTestToken(String userId, String email, String role, long expirationMillis) {
        return Jwts.builder()
                .claim("role", role)
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(testPrivateKey)
                .compact();
    }

    @Test
    void confirmTopUpAdminRoleReturns200() throws Exception {
        String token = generateTestToken(ADMIN_USER_ID, ADMIN_EMAIL, "ADMIN", 3600000);

        ConfirmTopUpResponse response = new ConfirmTopUpResponse(
                "tx-123",
                "SUCCESS",
                15000L,
                65000L,
                LocalDateTime.now()
        );

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(ADMIN_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(ADMIN_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn("ADMIN");
        when(transactionService.confirmTopUp("tx-123", ADMIN_USER_ID)).thenReturn(response);

        mockMvc.perform(post("/admin/wallet/topup/{transaction_id}/confirm", "tx-123")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_id").value("tx-123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(15000))
                .andExpect(jsonPath("$.new_balance").value(65000))
                .andExpect(jsonPath("$.confirmed_at").exists());
    }

    @Test
    void confirmTopUpNonAdminRoleReturns403() throws Exception {
        String token = generateTestToken("user-1", "user@example.com", "CUSTOMER", 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn("user-1");
        when(jwtUtil.getEmailFromToken(token)).thenReturn("user@example.com");
        when(jwtUtil.getRoleFromToken(token)).thenReturn("CUSTOMER");

        mockMvc.perform(post("/admin/wallet/topup/{transaction_id}/confirm", "tx-123")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not Authorized"));
    }

    @Test
    void getPendingTopUpRequestsAdminRoleReturns200() throws Exception {
        String token = generateTestToken(ADMIN_USER_ID, ADMIN_EMAIL, "ADMIN", 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(ADMIN_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(ADMIN_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn("ADMIN");
        when(transactionService.getPendingTransactions()).thenReturn(List.of(
            new PendingTransactionResponse("tx-1", 10000L, LocalDateTime.now()),
            new PendingTransactionResponse("tx-2", 20000L, LocalDateTime.now())
        ));

        mockMvc.perform(get("/admin/topup-request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction_id").value("tx-1"))
                .andExpect(jsonPath("$[0].amount").value(10000))
                .andExpect(jsonPath("$[0].created_at").exists())
                .andExpect(jsonPath("$[1].transaction_id").value("tx-2"))
                .andExpect(jsonPath("$[1].amount").value(20000))
                .andExpect(jsonPath("$[1].created_at").exists());
    }

    @Test
    void getPendingTopUpRequestsNonAdminRoleReturns403() throws Exception {
        String token = generateTestToken("user-1", "user@example.com", "CUSTOMER", 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn("user-1");
        when(jwtUtil.getEmailFromToken(token)).thenReturn("user@example.com");
        when(jwtUtil.getRoleFromToken(token)).thenReturn("CUSTOMER");

        mockMvc.perform(get("/admin/topup-request")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not Authorized"));
    }

    @Test
    void getWalletAdminRoleReturns200() throws Exception {
        String token = generateTestToken(ADMIN_USER_ID, ADMIN_EMAIL, "ADMIN", 3600000);
        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-1");
        wallet.setUserId(ADMIN_USER_ID);
        wallet.setBalance(100000L);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(ADMIN_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(ADMIN_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn("ADMIN");
        when(walletService.getWalletByUserId(ADMIN_USER_ID)).thenReturn(wallet);

        mockMvc.perform(get("/admin/wallets/{userQueryId}", "any-user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value("wallet-1"))
                .andExpect(jsonPath("$.userId").value(ADMIN_USER_ID));
    }

    @Test
    void getWalletRoleAdminPrefixReturns200() throws Exception {
        String token = generateTestToken(ADMIN_USER_ID, ADMIN_EMAIL, "ROLE_ADMIN", 3600000);
        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-2");
        wallet.setUserId(ADMIN_USER_ID);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(ADMIN_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(ADMIN_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn("ROLE_ADMIN");
        when(walletService.getWalletByUserId(ADMIN_USER_ID)).thenReturn(wallet);

        mockMvc.perform(get("/admin/wallets/{userQueryId}", "any-user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value("wallet-2"));
    }

    @Test
    void getWalletNonAdminReturns401FromGlobalHandler() throws Exception {
        String token = generateTestToken("user-1", "user@example.com", "CUSTOMER", 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn("user-1");
        when(jwtUtil.getEmailFromToken(token)).thenReturn("user@example.com");
        when(jwtUtil.getRoleFromToken(token)).thenReturn("CUSTOMER");

        mockMvc.perform(get("/admin/wallets/{userQueryId}", "any-user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not Authorized"));
    }

    @Test
    void getWalletNotFoundReturns404FromGlobalHandler() throws Exception {
        String token = generateTestToken(ADMIN_USER_ID, ADMIN_EMAIL, "ADMIN", 3600000);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getAccountIdFromToken(token)).thenReturn(ADMIN_USER_ID);
        when(jwtUtil.getEmailFromToken(token)).thenReturn(ADMIN_EMAIL);
        when(jwtUtil.getRoleFromToken(token)).thenReturn("ADMIN");
        when(walletService.getWalletByUserId(ADMIN_USER_ID))
                .thenThrow(new RuntimeException("Wallet not found for user: " + ADMIN_USER_ID));

        mockMvc.perform(get("/admin/wallets/{userQueryId}", "any-user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Wallet not found for user: " + ADMIN_USER_ID));
    }
}
