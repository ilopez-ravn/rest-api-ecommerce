package co.ravn.ecommerce.utils.enums;

public enum BillDocumentTypeEnum {
    RECEIPT("RECEIPT"),
    BILL("BILL");

    private final String value;

    BillDocumentTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
