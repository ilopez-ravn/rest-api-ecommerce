package co.ravn.ecommerce.dto.response.inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductStockResponse {
    private int id;
    private int product_id;
    private int warehouse_id;
    private int quantity;
    private LocalDateTime last_updated;
}
