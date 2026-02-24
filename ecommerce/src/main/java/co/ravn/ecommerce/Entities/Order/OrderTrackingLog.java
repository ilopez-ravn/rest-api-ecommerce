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
@Table(name = "order_tracking_log")
public class OrderTrackingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "delivery_tracking_id", referencedColumnName = "id")
    private DeliveryTracking deliveryTracking;

    @ManyToOne
    @JoinColumn(name = "previous_status_id", referencedColumnName = "id")
    private DeliveryStatus previousStatus;

    @ManyToOne
    @JoinColumn(name = "new_status_id", referencedColumnName = "id")
    private DeliveryStatus newStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by", referencedColumnName = "id")
    private SysUser changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    public OrderTrackingLog(DeliveryTracking deliveryTracking, DeliveryStatus previousStatus, DeliveryStatus newStatus, SysUser changedBy) {
        this.deliveryTracking = deliveryTracking;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
}
