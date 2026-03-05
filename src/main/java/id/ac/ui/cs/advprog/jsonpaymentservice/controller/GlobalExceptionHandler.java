package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.AmountMismatchResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.DuplicateRequestResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ErrorMessageResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.TransactionHasBeenConfirmedResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ValidationErrorResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.AmountMismatchException;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.DuplicateRequestException;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.TransactionHasBeenConfirmedException;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.ValidationErrorException;

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

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorMessageResponse> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageResponse(ex.getMessage()));
    }


    @ExceptionHandler(AuthorizationServiceException.class)
    public ResponseEntity<ErrorMessageResponse> handleNotAuthorized(AuthorizationServiceException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorMessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(AmountMismatchException.class)
    public ResponseEntity<AmountMismatchResponse> handleAmountMismatch(AmountMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AmountMismatchResponse(ex.getMessage(), ex.getExpected(), ex.getReceived()));
    }

    @ExceptionHandler(TransactionHasBeenConfirmedException.class)
    public ResponseEntity<TransactionHasBeenConfirmedResponse> handleConfirmedTransaction(TransactionHasBeenConfirmedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new TransactionHasBeenConfirmedResponse(ex.getMessage(), ex.getStatus()));
    }
}
