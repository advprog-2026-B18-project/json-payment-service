package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

import java.time.LocalDateTime;

public record UserTopUpTransactionResponse(
        String transaction_id,
        Long amount,
        String status,
        LocalDateTime created_at
) {
}