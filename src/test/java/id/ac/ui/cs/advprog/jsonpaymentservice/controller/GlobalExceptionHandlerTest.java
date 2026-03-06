package id.ac.ui.cs.advprog.jsonpaymentservice.controller;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.AmountMismatchResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.DuplicateRequestResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ErrorMessageResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.TransactionHasBeenConfirmedResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ValidationErrorResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.AmountMismatchException;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.DuplicateRequestException;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.TransactionHasBeenConfirmedException;
import id.ac.ui.cs.advprog.jsonpaymentservice.exception.ValidationErrorException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDuplicateReturns409() {
        ResponseEntity<DuplicateRequestResponse> response =
                handler.handleDuplicate(new DuplicateRequestException("Duplicate request", "tx-1"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Duplicate request", response.getBody().message());
        assertEquals("tx-1", response.getBody().existing_transaction_id());
    }

    @Test
    void handleValidationReturns400() {
        ResponseEntity<ValidationErrorResponse> response =
                handler.handleValidation(new ValidationErrorException("amount", "Minimum top-up is Rp 10.000"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("amount", response.getBody().error_field());
        assertEquals("Minimum top-up is Rp 10.000", response.getBody().message());
    }

    @Test
    void handleNotFoundReturns404() {
        ResponseEntity<ErrorMessageResponse> response =
                handler.handleNotFound(new NoSuchElementException("User not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().message());
    }

    @Test
    void handleNotAuthorizedReturns401() {
        ResponseEntity<ErrorMessageResponse> response =
                handler.handleNotAuthorized(new AuthorizationServiceException("Not Authorized"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not Authorized", response.getBody().message());
    }

    @Test
    void handleAmountMismatchReturns400() {
        ResponseEntity<AmountMismatchResponse> response =
                handler.handleAmountMismatch(new AmountMismatchException("Amount mismatch", 10000L, 5000L));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount mismatch", response.getBody().message());
        assertEquals(10000L, response.getBody().expected());
        assertEquals(5000L, response.getBody().received());
    }

    @Test
    void handleConfirmedTransactionReturns400() {
        ResponseEntity<TransactionHasBeenConfirmedResponse> response =
                handler.handleConfirmedTransaction(new TransactionHasBeenConfirmedException("Transaction has been confirmed", "SUCCESS"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Transaction has been confirmed", response.getBody().message());
        assertEquals("SUCCESS", response.getBody().status());
    }
}
