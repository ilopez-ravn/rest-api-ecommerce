package co.ravn.ecommerce.dto.response.inventory;

import co.ravn.ecommerce.entities.inventory.Tag;
import lombok.*;
import lombok.Builder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TagResponse {
    private int id;
    private String name;
    private Boolean is_active;

    public TagResponse(Tag tag) {
        id = tag.getId();
        name = tag.getName();
        is_active = tag.getIsActive();
    }
}
