package co.ravn.ecommerce.DTO.Request.Inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductImageUpdate {
    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_primary_image")
    private Boolean isPrimaryImage;
    
}
