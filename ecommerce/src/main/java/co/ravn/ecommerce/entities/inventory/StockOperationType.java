package co.ravn.ecommerce.entities.inventory;

public enum StockOperationType {
    ADD("ADD"),
    SUBTRACT("SUBTRACT");

    private final String value;

    StockOperationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
