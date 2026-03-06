package id.ac.ui.cs.advprog.jsonpaymentservice.service;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.WalletMinimalResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void testCreateWalletForUser() {
        String userId = "user-123";
        Wallet mockSavedWallet = new Wallet();
        mockSavedWallet.setUserId(userId);

        when(walletRepository.save(any(Wallet.class))).thenReturn(mockSavedWallet);

        Wallet result = walletService.createWalletForUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testGetWalletByUserIdSuccess() {
        // Arrange
        String userId = "user-123";
        Wallet mockWallet = new Wallet();
        mockWallet.setUserId(userId);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(mockWallet));

        Wallet result = walletService.getWalletByUserId(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(walletRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetWalletByUserIdThrowsException() {
        String userId = "invalid-user";
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            walletService.getWalletByUserId(userId);
        });

        assertEquals("Wallet not found for user: " + userId, exception.getMessage());
        verify(walletRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testProcessGetCurrentUserWalletCreatesWhenMissing() {
        String userId = "new-user";
        Wallet created = new Wallet();
        created.setWalletId("wallet-new");
        created.setUserId(userId);
        created.setBalance(0L);
        created.setEscrowBalance(0L);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(created);

        WalletMinimalResponse response = walletService.processGetCurrentUserWallet(userId);

        assertEquals("wallet-new", response.wallet_id());
        assertEquals(userId, response.user_id());
        assertEquals(0L, response.balance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
}
