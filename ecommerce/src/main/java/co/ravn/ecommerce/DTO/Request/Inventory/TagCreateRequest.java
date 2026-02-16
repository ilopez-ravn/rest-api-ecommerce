package co.ravn.ecommerce.DTO.Request.Inventory;

public class TagCreateRequest {
    private String name;

    public TagCreateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
