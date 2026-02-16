package co.ravn.ecommerce.DTO.Request.Inventory;

public class TagUpdateRequest {
    private String name;

    public TagUpdateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
