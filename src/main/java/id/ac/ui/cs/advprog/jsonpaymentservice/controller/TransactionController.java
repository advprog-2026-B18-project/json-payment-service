package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.DuplicateRequestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.TransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ValidationErrorResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.request.TopUpRequest;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.TransactionService;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/topup")
    public ResponseEntity<?> requestTopUp(
            @RequestAttribute("X-User-Id") String userId,
            @RequestBody TopUpRequest topUpRequest
    ) {
        try {
            TransactionResponse response = transactionService.processRequestTopUp(topUpRequest, userId);
            return ResponseEntity.status(201).body(response);
        } catch (TransactionService.MinimumTopUpException ex) {
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("amount", "Minimum top-up is Rp 10.000"));
        } catch (TransactionService.DuplicateRequestException ex) {
            return ResponseEntity.status(409)
                    .body(new DuplicateRequestResponse("Duplicate request", ex.getExistingTransactionId()));
        }
    }
}
