package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction;

public record ValidationErrorResponse(
    String error_field, 
    String message
){
}