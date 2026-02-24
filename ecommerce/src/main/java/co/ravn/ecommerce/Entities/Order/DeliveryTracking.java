package co.ravn.ecommerce.Entities.Order;

import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Clients.ClientAddress;
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
@Table(name = "delivery_tracking")
public class DeliveryTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private SaleOrder order;

    @ManyToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private ClientAddress address;

    @ManyToOne
    @JoinColumn(name = "carrier_id", referencedColumnName = "id")
    private Carrier carrier;

    @ManyToOne
    @JoinColumn(name = "assigned_to", referencedColumnName = "id")
    private SysUser assignedTo;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private DeliveryStatus status;

    @Column(name = "tracking_number", unique = true, nullable = false)
    private String trackingNumber;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DeliveryTracking(SaleOrder order, ClientAddress address, Carrier carrier, SysUser assignedTo, DeliveryStatus status, String trackingNumber, LocalDateTime estimatedDeliveryDate) {
        this.order = order;
        this.address = address;
        this.carrier = carrier;
        this.assignedTo = assignedTo;
        this.status = status;
        this.trackingNumber = trackingNumber;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
