package co.ravn.ecommerce.DTO.Request.Inventory;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TagCreateRequest {
    private String name;

    public TagCreateRequest(String name) {
        this.name = name;
    }

}
