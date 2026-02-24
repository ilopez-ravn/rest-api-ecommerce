package co.ravn.ecommerce.Utils.enums;

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
