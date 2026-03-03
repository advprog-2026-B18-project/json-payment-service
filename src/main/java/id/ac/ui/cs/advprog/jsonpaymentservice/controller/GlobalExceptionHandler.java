package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.DuplicateRequestResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ValidationErrorResponse;
import id.ac.ui.exception.DuplicateRequestException;
import id.ac.ui.exception.ValidationErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<DuplicateRequestResponse> handleDuplicate(DuplicateRequestException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new DuplicateRequestResponse(ex.getMessage(), ex.getTransactionId()));
    }

    @ExceptionHandler(ValidationErrorException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(ValidationErrorException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse(ex.getMessage(), ex.getErrorField()));
    }
}
