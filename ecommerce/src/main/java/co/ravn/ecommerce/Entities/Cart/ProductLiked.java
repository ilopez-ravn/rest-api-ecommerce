package co.ravn.ecommerce.Entities.Cart;

import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Inventory.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_liked")
public class ProductLiked {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SysUser user;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "has_been_notified")
    private Boolean hasBeenNotified;

    @Column(name = "liked_at")
    private LocalDateTime likedAt;

    public ProductLiked(SysUser user, Product product, Boolean hasBeenNotified) {
        this.user = user;
        this.product = product;
        this.hasBeenNotified = hasBeenNotified;
        this.likedAt = LocalDateTime.now();
    }
}
