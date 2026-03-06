package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.DeductPaymentResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.JwtAuthenticationFilter;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.JwtUtil;
import id.ac.ui.cs.advprog.jsonpaymentservice.security.SecurityConfig;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = InternalWalletController.class, properties = "app.internal.service-key=change-me")
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class InternalWalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void deductWalletBalanceSuccessReturns200() throws Exception {
        DeductPaymentResponse response = new DeductPaymentResponse(
                "tx-1",
                "PAYMENT",
                "user-1",
                150000L,
                50000L,
                170000L,
                "SUCCESS");

        when(transactionService.processInternalDeduct(any())).thenReturn(response);

        mockMvc.perform(post("/internal/wallets/deduct")
                .header("X-Service-Key", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "user_id": "user-1",
                          "order_id": "order-1",
                          "amount": 150000,
                          "description": "Order payment"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_id").value("tx-1"))
                .andExpect(jsonPath("$.type").value("PAYMENT"))
                .andExpect(jsonPath("$.user_id").value("user-1"))
                .andExpect(jsonPath("$.amount").value(150000))
                .andExpect(jsonPath("$.new_balance").value(50000))
                .andExpect(jsonPath("$.escrow_balance").value(170000))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void deductWalletBalanceDuplicateReturns409() throws Exception {
        when(transactionService.processInternalDeduct(any()))
                .thenThrow(new TransactionService.PaymentAlreadyProcessedException("tx-existing"));

        mockMvc.perform(post("/internal/wallets/deduct")
                .header("X-Service-Key", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "user_id": "user-1",
                          "order_id": "order-1",
                          "amount": 150000,
                          "description": "Order payment"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Payment already processed"))
                .andExpect(jsonPath("$.transaction_id").value("tx-existing"));
    }

    @Test
    void deductWalletBalanceInsufficientReturns422() throws Exception {
        when(transactionService.processInternalDeduct(any()))
                .thenThrow(new TransactionService.InsufficientBalanceException(80000L, 150000L));

        mockMvc.perform(post("/internal/wallets/deduct")
                .header("X-Service-Key", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "user_id": "user-1",
                          "order_id": "order-1",
                          "amount": 150000,
                          "description": "Order payment"
                        }
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insufficient balance"))
                .andExpect(jsonPath("$.balance").value(80000))
                .andExpect(jsonPath("$.required").value(150000));
    }

    @Test
    void deductWalletBalanceUserNotFoundReturns404() throws Exception {
        when(transactionService.processInternalDeduct(any()))
                .thenThrow(new TransactionService.UserNotFoundException());

        mockMvc.perform(post("/internal/wallets/deduct")
                .header("X-Service-Key", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "user_id": "unknown-user",
                          "order_id": "order-1",
                          "amount": 150000,
                          "description": "Order payment"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void deductWalletBalanceWrongServiceKeyReturns401() throws Exception {
        mockMvc.perform(post("/internal/wallets/deduct")
                .header("X-Service-Key", "wrong-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                                "user_id": "user-1",
                                "order_id": "order-1",
                                "amount": 150000,
                                "description": "Order payment"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }
}
