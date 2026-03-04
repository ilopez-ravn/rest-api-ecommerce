package co.ravn.ecommerce.dto.response.inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class WarehouseResponse {
    private int id;
    private String name;
    private String location;
    private Boolean is_active;
    private LocalDateTime created_at;
    private LocalDateTime last_updated;
}
