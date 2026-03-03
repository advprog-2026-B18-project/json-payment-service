package id.ac.ui.cs.advprog.jsonpaymentservice.exception;

public class DuplicateRequestException extends RuntimeException {
    private final String transactionId;
    
    public DuplicateRequestException(String msg, String id) {
        super(msg);
        this.transactionId = id;
    }

    public String getTransactionId() { 
        return transactionId;
    }
}
