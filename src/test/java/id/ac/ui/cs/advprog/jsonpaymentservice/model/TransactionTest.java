package id.ac.ui.cs.advprog.jsonpaymentservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
    }

    @Test
    void testPrePersistSetsTimestamps() {
        assertNull(transaction.getCreatedAt());
        assertNull(transaction.getUpdatedAt());

        transaction.onCreate();

        assertNotNull(transaction.getCreatedAt());
        assertNotNull(transaction.getUpdatedAt());

        assertEquals(transaction.getCreatedAt(), transaction.getUpdatedAt());
    }

    @Test
    void testPreUpdateChangesUpdatedAt() throws InterruptedException {
        transaction.onCreate();
        var initialCreatedAt = transaction.getCreatedAt();
        var initialUpdatedAt = transaction.getUpdatedAt();

        Thread.sleep(10);

        transaction.onUpdate();

        assertEquals(initialCreatedAt, transaction.getCreatedAt());
        assertNotNull(transaction.getUpdatedAt());
        assertTrue(transaction.getUpdatedAt().isAfter(initialUpdatedAt));
    }
}