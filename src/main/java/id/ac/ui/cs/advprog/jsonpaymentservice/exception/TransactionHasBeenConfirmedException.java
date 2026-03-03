package id.ac.ui.cs.advprog.jsonpaymentservice.exception;

public class TransactionHasBeenConfirmedException extends RuntimeException {
    private final String status;
    
    public TransactionHasBeenConfirmedException(String msg, String status) {
        super(msg);
        this.status = status;
    }

    public String getStatus() { 
        return status;
    }
}
