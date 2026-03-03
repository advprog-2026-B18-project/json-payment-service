package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    // TODO: validasi role = admin (admin doang yg boleh akses semua endpoint disini)

    @GetMapping("/wallets/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable String userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        
        // TODO: validasi user Id, return 404 not found kalo gaada wallet dg user id bersesuaian 
        
        return ResponseEntity.ok(wallet);
    }

}
