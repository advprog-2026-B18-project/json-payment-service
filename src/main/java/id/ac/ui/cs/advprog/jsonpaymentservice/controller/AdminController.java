package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.WalletService;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final WalletService walletService;

    public AdminController(WalletService walletService) {
        this.walletService = walletService;
    }
    // TODO: validasi role = admin (admin doang yg boleh akses semua endpoint
    // disini)

    @GetMapping("/wallets/{userQueryId}")
    public ResponseEntity<Wallet> getWallet(
            @RequestAttribute("X-User-Id") String userId,
            @RequestAttribute("X-Username") String username,
            @RequestAttribute("X-Role") String role,
            @PathVariable String userQueryId
        ) {

        if (role != "ROLE_ADMIN"){
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

}
