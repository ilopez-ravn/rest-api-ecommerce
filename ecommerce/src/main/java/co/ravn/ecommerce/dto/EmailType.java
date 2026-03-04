package co.ravn.ecommerce.dto;

public enum EmailType {
    PASSWORD_RECOVERY("PASSWORD_RECOVERY"),
    PRODUCT_LIKED_ALERT("PRODUCT_LIKED_ALERT"),
    ORDER_CONFIRMATION("ORDER_CONFIRMATION"),
    DELIVERY_STATUS_UPDATE("DELIVERY_STATUS_UPDATE"),
    REFUND_REQUESTED("REFUND_REQUESTED"),
    REFUND_APPROVED("REFUND_APPROVED"),
    REFUND_DENIED("REFUND_DENIED"),
    RETURN_IN_TRANSIT("RETURN_IN_TRANSIT"),
    RETURN_RECEIVED("RETURN_RECEIVED"),
    REFUND_PROCESSED("REFUND_PROCESSED");

    private final String value;

    EmailType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
