package id.ac.ui.cs.advprog.jsonpaymentservice.service;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void testGetWalletByUserId_Success() {
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
}
