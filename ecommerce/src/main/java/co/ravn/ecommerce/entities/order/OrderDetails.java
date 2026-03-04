package co.ravn.ecommerce.entities.order;

import co.ravn.ecommerce.entities.inventory.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_details")
public class OrderDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private SaleOrder order;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "tax_percent", nullable = false)
    private Integer taxPercent;

    @Column(name = "total_amount", insertable = false, updatable = false)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", insertable = false, updatable = false)
    private BigDecimal taxAmount;

    public OrderDetails(SaleOrder order, Product product, BigDecimal price, Integer quantity, Integer taxPercent) {
        this.order = order;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.taxPercent = taxPercent;
    }
}
