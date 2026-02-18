package co.ravn.ecommerce.DTO.Request.Inventory;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ProductUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;

    @JsonProperty("category_list")
    private List<Integer> categoryList;
    
    @JsonProperty("tag_list")
    private List <Integer> tagList;

    @JsonProperty("image_list")
    private List<ProductImageUpdate> imageList;

    @JsonProperty("is_active")
    private Boolean isActive;
}
