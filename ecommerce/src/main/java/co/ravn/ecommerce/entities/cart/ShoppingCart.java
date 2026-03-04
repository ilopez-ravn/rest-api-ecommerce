package co.ravn.ecommerce.entities.cart;

import co.ravn.ecommerce.entities.auth.Person;
import co.ravn.ecommerce.utils.enums.ShoppingCartStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "shopping_cart")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Person client;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "shopping_cart_status_enum")
    private ShoppingCartStatusEnum status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // @OneToMany
    // @JoinTable(name = "shopping_cart_details", joinColumns = @JoinColumn(name =
    // "cart_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    // List<ShoppingCartDetails> products;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingCartDetails> products;

    public ShoppingCart(Person client, ShoppingCartStatusEnum status) {
        this.client = client;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setProducts(List<ShoppingCartDetails> products) {
        if (products != null) {
            this.products = products.stream()
                    .map(cartRequest -> {
                        ShoppingCartDetails detail = new ShoppingCartDetails();
                        detail.setCart(this);
                        detail.setProduct(cartRequest.getProduct());
                        detail.setPrice(cartRequest.getPrice());
                        detail.setQuantity(cartRequest.getQuantity());
                        detail.setAddedAt(LocalDateTime.now());
                        detail.setUpdatedAt(LocalDateTime.now());
                        return detail;
                    })
                    .collect(Collectors.toList());
        } else {
            this.products = null;
        }
    }
}
