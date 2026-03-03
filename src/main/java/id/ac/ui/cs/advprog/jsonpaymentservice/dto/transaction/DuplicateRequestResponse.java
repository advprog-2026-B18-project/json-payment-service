package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

public record DuplicateRequestResponse (
    String message,
    String existingTransactionId
){    
}
