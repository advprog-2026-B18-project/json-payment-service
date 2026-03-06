package id.ac.ui.cs.advprog.jsonpaymentservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomExceptionsTest {

    @Test
    void amountMismatchExceptionStoresFields() {
        AmountMismatchException exception = new AmountMismatchException("Amount mismatch", 10000L, 8000L);

        assertEquals("Amount mismatch", exception.getMessage());
        assertEquals(10000L, exception.getExpected());
        assertEquals(8000L, exception.getReceived());
    }

    @Test
    void duplicateRequestExceptionStoresFields() {
        DuplicateRequestException exception = new DuplicateRequestException("Duplicate request", "tx-123");

        assertEquals("Duplicate request", exception.getMessage());
        assertEquals("tx-123", exception.getTransactionId());
    }

    @Test
    void validationErrorExceptionStoresFields() {
        ValidationErrorException exception = new ValidationErrorException("Invalid amount", "amount");

        assertEquals("Invalid amount", exception.getMessage());
        assertEquals("amount", exception.getErrorField());
    }

    @Test
    void transactionHasBeenConfirmedExceptionStoresFields() {
        TransactionHasBeenConfirmedException exception =
                new TransactionHasBeenConfirmedException("Transaction has been confirmed", "SUCCESS");

        assertEquals("Transaction has been confirmed", exception.getMessage());
        assertEquals("SUCCESS", exception.getStatus());
    }
}
