package co.ravn.ecommerce.Entities.Inventory;

import jakarta.persistence.*;

@Entity
@Table(name = "product_image")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_primary_image")
    private Boolean isPrimaryImage;

    public ProductImage() {
    }

    public ProductImage(int id, String imageUrl, Boolean isPrimaryImage) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isPrimaryImage = isPrimaryImage;
    }

    public ProductImage(String imageUrl, Boolean isPrimaryImage) {
        this.imageUrl = imageUrl;
        this.isPrimaryImage = isPrimaryImage;
    }
}
