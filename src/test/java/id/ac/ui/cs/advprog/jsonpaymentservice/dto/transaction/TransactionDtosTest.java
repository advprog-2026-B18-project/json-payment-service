package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionDtosTest {

    @Test
    void amountMismatchResponseRecordWorks() {
        AmountMismatchResponse response = new AmountMismatchResponse("Amount mismatch", 10000L, 9000L);

        assertEquals("Amount mismatch", response.message());
        assertEquals(10000L, response.expected());
        assertEquals(9000L, response.received());
    }

    @Test
    void transactionHasBeenConfirmedResponseRecordWorks() {
        TransactionHasBeenConfirmedResponse response =
                new TransactionHasBeenConfirmedResponse("Transaction has been confirmed", "SUCCESS");

        assertEquals("Transaction has been confirmed", response.message());
        assertEquals("SUCCESS", response.status());
    }
}
