package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class WarehouseInfoResponse {
    private int id;
    private String name;
    private String location;
    private Boolean is_active;
    private LocalDateTime created_at;
}
