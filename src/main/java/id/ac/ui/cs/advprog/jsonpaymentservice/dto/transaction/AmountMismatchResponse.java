package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

public record AmountMismatchResponse (
    String message,
    Long expected, 
    Long received
){

}
