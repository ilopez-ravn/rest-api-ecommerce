package co.ravn.ecommerce.dto.request.inventory;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
