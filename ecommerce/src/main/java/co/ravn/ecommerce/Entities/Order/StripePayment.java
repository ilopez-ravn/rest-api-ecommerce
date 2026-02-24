package co.ravn.ecommerce.Entities.Order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stripe_payment")
public class StripePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private SaleOrder order;

    @Column(name = "stripe_payment_id", unique = true, nullable = false)
    private String stripePaymentId;

    @Column(name = "client_secret_key", nullable = false)
    private String clientSecretKey;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_method_types", nullable = false)
    private List<String> paymentMethodTypes;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public StripePayment(SaleOrder order, String stripePaymentId, String clientSecretKey, String paymentMethod, List<String> paymentMethodTypes, BigDecimal amount, String currency, String paymentStatus) {
        this.order = order;
        this.stripePaymentId = stripePaymentId;
        this.clientSecretKey = clientSecretKey;
        this.paymentMethod = paymentMethod;
        this.paymentMethodTypes = paymentMethodTypes;
        this.amount = amount;
        this.currency = currency;
        this.paymentStatus = paymentStatus;
        this.createdAt = LocalDateTime.now();
    }
}
