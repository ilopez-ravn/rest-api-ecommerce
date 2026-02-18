package co.ravn.ecommerce.DTO.Response.Inventory;

import co.ravn.ecommerce.Entities.Inventory.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ProductImageResponse {
    private int id;
    private String image_url;
    private int product_id;
    private Boolean is_primary_image;
    private Boolean is_active;
    private LocalDateTime created_at;

    public ProductImageResponse(ProductImage productImage) {
        this.id = productImage.getId();
        this.image_url = productImage.getImageUrl();
        this.product_id = productImage.getProductId();
        this.is_primary_image = productImage.getIsPrimaryImage();
        this.is_active = productImage.getIsActive();
        this.created_at = productImage.getCreatedAt();
    }
}
