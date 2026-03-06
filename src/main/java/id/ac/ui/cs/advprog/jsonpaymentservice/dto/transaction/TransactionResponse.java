package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

import java.time.LocalDateTime;

public record TransactionResponse (
    String transaction_id,
    String type,
    Long amount,
    String status,
    LocalDateTime created_at
) {

}
