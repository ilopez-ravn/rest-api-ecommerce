package co.ravn.ecommerce.DTO.Request.Inventory;

public class CategoryCreateRequest {
    private String name;
    private String description;

    public CategoryCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CategoryCreateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
