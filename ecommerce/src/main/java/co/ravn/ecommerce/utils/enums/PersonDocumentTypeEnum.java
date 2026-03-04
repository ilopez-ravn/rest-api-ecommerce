package co.ravn.ecommerce.utils.enums;

public enum PersonDocumentTypeEnum {
    PERSON("PERSON"),
    BUSINESS("BUSINESS");

    private final String value;

    PersonDocumentTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
