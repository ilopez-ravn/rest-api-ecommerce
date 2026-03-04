package co.ravn.ecommerce.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageResponse {
    private int id;
    private String image_url;
    private int product_id;
    private String public_id;
    private Boolean is_primary_image;
    private Boolean is_active;
    private LocalDateTime created_at;
}
