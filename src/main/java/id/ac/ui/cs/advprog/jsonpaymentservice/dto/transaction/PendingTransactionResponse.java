package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

public record PendingTransactionResponse(
        String transaction_id,
        Long amount
) {
}