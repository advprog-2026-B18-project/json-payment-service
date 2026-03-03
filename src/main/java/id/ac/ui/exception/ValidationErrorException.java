package id.ac.ui.exception;

public class ValidationErrorException extends RuntimeException {
    private final String errorField;
    
    public ValidationErrorException(String msg, String errorField) {
        super(msg);
        this.errorField = errorField;
    }

    public String getErrorField() { 
        return errorField;
    }
}
