package co.ravn.ecommerce.Entities.Inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "product_image")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "product_id")
    private int productId;

    @Column(name = "is_primary_image")
    private Boolean isPrimaryImage;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ProductImage() {
    }

    public ProductImage(int id, String imageUrl, Boolean isPrimaryImage, int productId) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isPrimaryImage = isPrimaryImage;
        this.productId = productId;
    }

    public ProductImage(String imageUrl, Boolean isPrimaryImage) {
        this.imageUrl = imageUrl;
        this.isPrimaryImage = isPrimaryImage;
    }
}
