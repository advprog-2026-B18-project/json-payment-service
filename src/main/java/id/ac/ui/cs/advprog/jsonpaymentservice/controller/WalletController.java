package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.WalletMinimalResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // TODO: implement validasi jwt dulu wak
    // TODO: tambain param buat get jwt nya (?)
    @GetMapping("/me")
    public ResponseEntity<WalletMinimalResponse> getCurrentUserWallet() {
      
      // TODO: validasi jwt nya, kalo ga valid return 401 unauthorized
      // TODO: kalo valid, extract user id nya, taro di param getWalletByUserId
      Wallet wallet = walletService.getWalletByUserId("userId"); 

      long withdrawable_balance = wallet.getBalance() - wallet.getEscrowBalance();
      WalletMinimalResponse response = new WalletMinimalResponse(wallet.getWalletId(), wallet.getUserId(), withdrawable_balance); 
      
      return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Wallet> createWallet(@PathVariable String userId) {
        Wallet newWallet = walletService.createWalletForUser(userId);
        return new ResponseEntity<>(newWallet, HttpStatus.CREATED);
    }
}