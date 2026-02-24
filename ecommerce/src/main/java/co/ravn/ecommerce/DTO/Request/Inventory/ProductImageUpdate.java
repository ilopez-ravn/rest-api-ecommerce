package co.ravn.ecommerce.DTO.Request.Inventory;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProductImageUpdate {
    @JsonProperty("image_url")
    @NotBlank(message = "Image URL is required")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    @JsonProperty("is_primary_image")
    private Boolean isPrimaryImage;

    @JsonProperty("public_id")
    @NotBlank(message = "Public ID is required")
    private String publicId;
}
