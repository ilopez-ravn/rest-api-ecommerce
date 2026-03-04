package co.ravn.ecommerce.utils.enums;

public enum ShoppingCartStatusEnum {
    ACTIVE("ACTIVE"),
    DELETED("DELETED"),
    PROCESSED("PROCESSED");

    private final String value;

    ShoppingCartStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
