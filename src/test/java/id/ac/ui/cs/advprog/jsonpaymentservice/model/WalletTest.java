package id.ac.ui.cs.advprog.jsonpaymentservice.model;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalletTest {

    @Test
    void testPrePersistSetsTimestamps() {
        Wallet wallet = new Wallet();
        assertNull(wallet.getCreatedAt());
        assertNull(wallet.getUpdatedAt());

        // Manually trigger the lifecycle method for unit testing
        wallet.onCreate();

        LocalDateTime createdAt = wallet.getCreatedAt();
        LocalDateTime updatedAt = wallet.getUpdatedAt();

        assertNotNull(createdAt);
        assertNotNull(updatedAt);
        assertTrue(createdAt.isBefore(updatedAt) || createdAt.isEqual(updatedAt));
    }

    @Test
    void testPreUpdateChangesUpdatedAt() throws InterruptedException {
        Wallet wallet = new Wallet();
        wallet.onCreate();

        var initialUpdatedAt = wallet.getUpdatedAt();

        // Slight delay to ensure the timestamp changes
        Thread.sleep(10);

        wallet.onUpdate();

        assertNotNull(wallet.getUpdatedAt());
        assertTrue(wallet.getUpdatedAt().isAfter(initialUpdatedAt));
    }
}
