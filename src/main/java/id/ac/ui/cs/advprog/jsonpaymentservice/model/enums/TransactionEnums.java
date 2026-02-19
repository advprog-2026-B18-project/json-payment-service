package id.ac.ui.cs.advprog.jsonpaymentservice.model.enums;

public class TransactionEnums {
    public enum Type {
        TOPUP, PAYMENT, REFUND, EARNING, WITHDRAWAL, ADJUSTMENT
    }

    public enum Direction {
        CREDIT, DEBIT
    }

    public enum Status {
        PENDING, SUCCESS, FAILED, CANCELLED
    }

    public enum ReferenceType {
        ORDER, TOPUP, WITHDRAWAL
    }
}