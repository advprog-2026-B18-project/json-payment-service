package id.ac.ui.cs.advprog.jsonpaymentservice.service;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.WalletMinimalResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWalletForUser(String userId) {
        Wallet wallet = new Wallet();
        wallet.setWalletId(UUID.randomUUID().toString());
        wallet.setUserId(userId);

        return walletRepository.save(wallet);
    }

    public Wallet getWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
    }

    public WalletMinimalResponse processGetCurrentUserWallet(String userId) {
        Wallet wallet;
        try {
            wallet = this.getWalletByUserId(userId);
        } catch (RuntimeException ex) {
            // cek apakah user ada di database
            
            wallet = this.createWalletForUser(userId);
        }

        long withdrawable_balance = wallet.getBalance() - wallet.getEscrowBalance();
        WalletMinimalResponse response = new WalletMinimalResponse(
                wallet.getWalletId(),
                wallet.getUserId(),
                withdrawable_balance
            );

        return response;
    }
}
