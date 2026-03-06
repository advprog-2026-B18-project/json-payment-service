package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

public record InsufficientBalanceResponse(
    String message,
    Long balance,
    Long required
) {
}