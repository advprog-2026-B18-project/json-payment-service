package id.ac.ui.cs.advprog.jsonpaymentservice.exception;

import lombok.Getter;

@Getter
public class AmountMismatchException extends RuntimeException {
    private final Long expected;
    private final Long received;
    
    public AmountMismatchException(String msg, Long expected, Long received) {
        super(msg);
        this.expected = expected;
        this.received = received;
    }
}
