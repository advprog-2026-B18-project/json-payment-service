package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.WalletMinimalResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.UserTopUpTransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.TransactionService;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    public WalletController(WalletService walletService, TransactionService transactionService) {
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @GetMapping("/me")
    public ResponseEntity<WalletMinimalResponse> getCurrentUserWallet(
            @RequestAttribute("X-User-Id") String userId,
            @RequestAttribute("X-Email") String email,
            @RequestAttribute("X-Role") String role
        ) {

        WalletMinimalResponse response = walletService.processGetCurrentUserWallet(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Wallet> createWallet(@PathVariable String userId) {
        Wallet newWallet = walletService.createWalletForUser(userId);
        return new ResponseEntity<>(newWallet, HttpStatus.CREATED);
    }

    @GetMapping("/me/topup")
    public ResponseEntity<List<UserTopUpTransactionResponse>> getCurrentUserTopUpTransactions(
            @RequestAttribute("X-User-Id") String userId
    ) {
        List<UserTopUpTransactionResponse> response = transactionService.getCurrentUserTopUpTransactions(userId);
        return ResponseEntity.ok(response);
    }
}
