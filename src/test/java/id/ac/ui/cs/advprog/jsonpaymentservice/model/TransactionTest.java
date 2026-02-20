package id.ac.ui.cs.advprog.jsonpaymentservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        LocalDateTime createdAt = transaction.getCreatedAt();
        LocalDateTime updatedAt = transaction.getUpdatedAt();

        assertNotNull(createdAt);
        assertNotNull(updatedAt);
        assertTrue(createdAt.isBefore(updatedAt) || createdAt.isEqual(updatedAt));
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