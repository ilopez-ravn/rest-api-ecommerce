package co.ravn.ecommerce.Entities.Order;

import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Clients.ClientAddress;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sale_order")
public class SaleOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Person client;

    @ManyToOne
    @JoinColumn(name = "shopping_cart_id", referencedColumnName = "id")
    private ShoppingCart shoppingCart;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id")
    private Warehouse warehouse;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refund_reason")
    private String refundReason;

    @OneToMany(mappedBy = "order")
    private List<DeliveryTracking> deliveryTrackings = new ArrayList<>();

    public SaleOrder(Person client, ShoppingCart shoppingCart, Warehouse warehouse, Boolean isActive) {
        this.client = client;
        this.shoppingCart = shoppingCart;
        this.warehouse = warehouse;
        this.orderDate = LocalDateTime.now();
        this.isActive = isActive;
    }
}
