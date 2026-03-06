package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

public record DeductPaymentResponse(
        String transaction_id,
        String type,
        String user_id,
        Long amount,
        Long new_balance,
        Long escrow_balance,
        String status
) {
}