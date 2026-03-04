package co.ravn.ecommerce.entities.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stripe_payment_event_log")
public class StripePaymentEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private StripePayment payment;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", nullable = false)
    private String eventData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public StripePaymentEventLog(StripePayment payment, String eventType, String status, String eventData) {
        this.payment = payment;
        this.eventType = eventType;
        this.status = status;
        this.eventData = eventData;
        this.createdAt = LocalDateTime.now();
    }
}
