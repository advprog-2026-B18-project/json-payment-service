package id.ac.ui.cs.advprog.jsonpaymentservice.repository;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void testFindByUserId_Success() {
        String userId = UUID.randomUUID().toString();
        Wallet wallet = new Wallet();
        wallet.setWalletId(UUID.randomUUID().toString());
        wallet.setUserId(userId);
        walletRepository.save(wallet);

        Optional<Wallet> foundWallet = walletRepository.findByUserId(userId);

        assertTrue(foundWallet.isPresent());
        assertEquals(userId, foundWallet.get().getUserId());
    }

    @Test
    void testFindByUserId_NotFound() {
        Optional<Wallet> foundWallet = walletRepository.findByUserId("non-existent-user");

        assertFalse(foundWallet.isPresent());
    }
}