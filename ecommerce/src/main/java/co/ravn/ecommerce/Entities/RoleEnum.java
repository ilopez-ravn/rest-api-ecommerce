package co.ravn.ecommerce.Entities;

public enum RoleEnum {
    MANAGER("MANAGER"),
    CLIENT("CLIENT"),
    WAREHOUSE("WAREHOUSE"),
    SHIPPING("SHIPPING");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
