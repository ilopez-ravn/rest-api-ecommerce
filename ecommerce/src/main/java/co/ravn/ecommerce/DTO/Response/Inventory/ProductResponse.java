package co.ravn.ecommerce.DTO.Response.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ProductResponse {
    @NotBlank
    private int id;
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private BigDecimal price;
    @NotBlank
    private Boolean is_active;
    @NotNull
    private List<CategoryResponse> categories;
    @NotNull
    private List<TagResponse> tags;
    @NotNull
    private List<ProductImageResponse> product_images;
    @NotNull
    private LocalDateTime created_at;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.is_active = product.getIsActive();
        this.categories = product.getCategories() == null
                ? Collections.emptyList()
                : product.getCategories().stream().map(CategoryResponse::new).collect(Collectors.toList());
        this.tags = product.getTags() == null
                ? Collections.emptyList()
                : product.getTags().stream().map(TagResponse::new).collect(Collectors.toList());
        this.product_images = product.getProductImages() == null
                ? Collections.emptyList()
                : product.getProductImages().stream().map(ProductImageResponse::new).collect(Collectors.toList());
        this.created_at = product.getCreatedAt();
    }
}
