package co.ravn.ecommerce.utils.enums;

public enum EmailStatusEnum {
    SENT("SENT"),
    NOT_SENT("NOT_SENT");

    private final String value;

    EmailStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
