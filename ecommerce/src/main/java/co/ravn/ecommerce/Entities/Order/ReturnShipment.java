package co.ravn.ecommerce.Entities.Order;

import co.ravn.ecommerce.Entities.Auth.SysUser;
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
@Table(name = "return_shipments")
public class ReturnShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "refund_request_id", referencedColumnName = "id", nullable = false)
    private RefundRequest refundRequest;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber;

    @Column(name = "carrier_name", nullable = false)
    private String carrierName;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @ManyToOne
    @JoinColumn(name = "received_by", referencedColumnName = "id")
    private SysUser receivedBy;
}
