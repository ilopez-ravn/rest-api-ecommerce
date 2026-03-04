package co.ravn.ecommerce.entities.order;

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
@Table(name = "delivery_status")
public class DeliveryStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(name = "step_order", unique = true, nullable = false)
    private Integer stepOrder;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public DeliveryStatus(String name, Integer stepOrder, String description) {
        this.name = name;
        this.stepOrder = stepOrder;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}
