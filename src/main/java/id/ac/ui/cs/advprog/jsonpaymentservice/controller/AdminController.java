package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import java.util.NoSuchElementException;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ConfirmTopUpResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ErrorMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.TransactionService;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.WalletService;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final WalletService walletService;
    private final TransactionService transactionService;

    public AdminController(WalletService walletService, TransactionService transactionService) {
        this.walletService = walletService;
        this.transactionService = transactionService;
    }
    // TODO: validasi role = admin (admin doang yg boleh akses semua endpoint
    // disini)

    @GetMapping("/wallets/{userQueryId}")
    public ResponseEntity<Wallet> getWallet(
            @RequestAttribute("X-User-Id") String userId,
            @RequestAttribute("X-Email") String username,
            @RequestAttribute("X-Role") String role,
            @PathVariable String userQueryId
        ) {

        if (!isAdminRole(role)) {
            throw new AuthorizationServiceException("Not Authorized");
        }
        
        Wallet wallet;

        try {
            wallet = walletService.getWalletByUserId(userId);
        } catch (RuntimeException ex){
            throw new NoSuchElementException(ex.getMessage());
        }

        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/wallet/topup/{transaction_id}/confirm")
    public ResponseEntity<?> confirmTopUp(
            @RequestAttribute("X-User-Id") String userId,
            @RequestAttribute("X-Role") String role,
            @PathVariable("transaction_id") String transactionId
    ) {
        if (!isAdminRole(role)) {
            return ResponseEntity.status(403).body(new ErrorMessageResponse("Not Authorized"));
        }

        ConfirmTopUpResponse response = transactionService.confirmTopUp(transactionId, userId);
        return ResponseEntity.ok(response);
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }

}
