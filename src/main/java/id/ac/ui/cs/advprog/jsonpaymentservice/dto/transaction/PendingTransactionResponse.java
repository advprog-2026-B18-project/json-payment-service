package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

import java.time.LocalDateTime;

public record PendingTransactionResponse(
        String transaction_id,
        Long amount,
        LocalDateTime created_at
) {
}