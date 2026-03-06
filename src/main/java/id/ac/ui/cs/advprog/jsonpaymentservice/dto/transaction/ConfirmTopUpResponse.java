package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

import java.time.LocalDateTime;

public record ConfirmTopUpResponse(
        String transaction_id,
        String status,
        Long amount,
        Long new_balance,
        LocalDateTime confirmed_at
) {
}