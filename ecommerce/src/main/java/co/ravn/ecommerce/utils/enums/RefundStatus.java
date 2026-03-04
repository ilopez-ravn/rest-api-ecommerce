package co.ravn.ecommerce.utils.enums;

public enum RefundStatus {
    PENDING_REVIEW("PENDING_REVIEW"),
    APPROVED("APPROVED"),
    DENIED("DENIED"),
    CANCELLED("CANCELLED"),
    RETURN_IN_TRANSIT("RETURN_IN_TRANSIT"),
    PRODUCT_RECEIVED("PRODUCT_RECEIVED"),
    REFUND_PROCESSED("REFUND_PROCESSED");

    private final String value;

    RefundStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
