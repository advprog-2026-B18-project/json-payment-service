package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.DeductPaymentResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ErrorMessageResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.InsufficientBalanceResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.PaymentAlreadyProcessedResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.request.InternalDeductRequest;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/wallets")
public class InternalWalletController {

    private final TransactionService transactionService;

    @Value("${app.internal.service-key:change-me}")
    private String internalServiceKey;

    public InternalWalletController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deduct")
    public ResponseEntity<?> deductWalletBalance(
            @RequestHeader(value = "X-Service-Key", required = false) String serviceKey,
            @RequestBody InternalDeductRequest request
    ) {
        if (serviceKey == null || !serviceKey.equals(internalServiceKey)) {
            return ResponseEntity.status(401).body(new ErrorMessageResponse("Unauthorized"));
        }

        try {
            DeductPaymentResponse response = transactionService.processInternalDeduct(request);
            return ResponseEntity.ok(response);
        } catch (TransactionService.PaymentAlreadyProcessedException ex) {
            return ResponseEntity.status(409)
                    .body(new PaymentAlreadyProcessedResponse("Payment already processed", ex.getTransactionId()));
        } catch (TransactionService.InsufficientBalanceException ex) {
            return ResponseEntity.status(422)
                    .body(new InsufficientBalanceResponse("Insufficient balance", ex.getBalance(), ex.getRequired()));
        } catch (TransactionService.UserNotFoundException ex) {
            return ResponseEntity.status(404)
                    .body(new ErrorMessageResponse("User not found"));
        }
    }
}