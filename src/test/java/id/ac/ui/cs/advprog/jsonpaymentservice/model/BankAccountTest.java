package id.ac.ui.cs.advprog.jsonpaymentservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BankAccountTest {

    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        bankAccount = new BankAccount();
    }

    @Test
    void testDefaultValuesAreSetCorrectly() {
        assertFalse(bankAccount.getIsVerified());
        assertFalse(bankAccount.getIsPrimary());
    }

    @Test
    void testPrePersistSetsCreatedAt() {
        assertNull(bankAccount.getCreatedAt());

        bankAccount.onCreate();

        assertNotNull(bankAccount.getCreatedAt());
    }
}