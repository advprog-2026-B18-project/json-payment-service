package id.ac.ui.cs.advprog.jsonpaymentservice.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void testPrePersistSetsTimestamps() {
        Wallet wallet = new Wallet();
        assertNull(wallet.getCreatedAt());
        assertNull(wallet.getUpdatedAt());

        // Manually trigger the lifecycle method for unit testing
        wallet.onCreate();

        assertNotNull(wallet.getCreatedAt());
        assertNotNull(wallet.getUpdatedAt());
        assertEquals(wallet.getCreatedAt(), wallet.getUpdatedAt());
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
