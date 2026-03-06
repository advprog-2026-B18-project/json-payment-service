package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

public record PaymentAlreadyProcessedResponse(
    String message,
    String transaction_id
) {
}