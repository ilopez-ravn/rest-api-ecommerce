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

import co.ravn.ecommerce.Utils.enums.BillDocumentTypeEnum;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_bill")
public class OrderBill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private SaleOrder order;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "document_type", nullable = false)
    private BillDocumentTypeEnum documentType;

    @Column(name = "document_number", unique = true, nullable = false)
    private String documentNumber;

    @Column(name = "tax_percent", nullable = false)
    private Integer taxPercent;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    @Column(name = "tax_amount", insertable = false, updatable = false)
    private BigDecimal taxAmount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    public OrderBill(SaleOrder order, BillDocumentTypeEnum documentType, String documentNumber, Integer taxPercent, BigDecimal totalAmount, BigDecimal deliveryFee, Boolean isActive) {
        this.order = order;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.taxPercent = taxPercent;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.isActive = isActive;
        this.issuedAt = LocalDateTime.now();
    }
}
