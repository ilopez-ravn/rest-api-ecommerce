package co.ravn.ecommerce.Entities.Inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String description;
    private BigDecimal price;

    @Column(name = "is_active")
    private Boolean isActive;

        @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
@JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    List<Category> categories;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "product_tag",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    List<Tag> tags;

    @OneToMany
    @JoinTable(
            name = "product_image",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    List<ProductImage> productImages;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Product() {
    }

    public Product(int id, String name, String description, BigDecimal price, Boolean isActive, List<Category> categories, List<Tag> tags, List<ProductImage> productImages, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
        this.categories = categories;
        this.tags = tags;
        this.productImages = productImages;
        this.createdAt = createdAt;
    }

    public Product(String name, String description, BigDecimal price, Boolean isActive, List<Category> categories, List<Tag> tags, List<ProductImage> productImages) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
        this.categories = categories;
        this.tags = tags;
        this.productImages = productImages;
    }

}
