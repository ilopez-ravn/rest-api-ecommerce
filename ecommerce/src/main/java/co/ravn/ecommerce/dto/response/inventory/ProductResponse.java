package co.ravn.ecommerce.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean is_active;
    private List<CategoryResponse> categories;
    private List<TagResponse> tags;
    private List<ProductImageResponse> product_images;
    private ProductStockResponse stock;
    private LocalDateTime created_at;
}
