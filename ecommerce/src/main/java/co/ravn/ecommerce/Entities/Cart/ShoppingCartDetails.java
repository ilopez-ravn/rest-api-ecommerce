package co.ravn.ecommerce.Entities.Cart;

import co.ravn.ecommerce.Entities.Inventory.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shopping_cart_details")
public class ShoppingCartDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "cart_id", referencedColumnName = "id", nullable = false)
    private ShoppingCart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(insertable = false, updatable = false)
    private BigDecimal total;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ShoppingCartDetails(ShoppingCart cart, Product product, BigDecimal price, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
