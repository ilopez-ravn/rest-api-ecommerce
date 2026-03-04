package co.ravn.ecommerce.entities.order;

import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.utils.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private SaleOrder order;

    @ManyToOne
    @JoinColumn(name = "requested_by", referencedColumnName = "id", nullable = false)
    private SysUser requestedBy;

    @ManyToOne
    @JoinColumn(name = "reviewed_by", referencedColumnName = "id")
    private SysUser reviewedBy;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private RefundStatus status;

    @Column(name = "requires_return", nullable = false)
    private boolean requiresReturn;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "manager_notes", columnDefinition = "TEXT")
    private String managerNotes;

    @Column(name = "stripe_refund_id")
    private String stripeRefundId;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @OneToOne(mappedBy = "refundRequest")
    private ReturnShipment returnShipment;
}
