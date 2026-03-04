package co.ravn.ecommerce.exception;

public class PaymentFailureException extends RuntimeException {
    public PaymentFailureException(String message) {
        super(message);
    }

    public PaymentFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
