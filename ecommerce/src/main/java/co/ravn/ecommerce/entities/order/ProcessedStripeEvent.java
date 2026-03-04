package co.ravn.ecommerce.entities.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "processed_stripe_event")
public class ProcessedStripeEvent {

    @Id
    private String id;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public ProcessedStripeEvent(String id) {
        this.id = id;
        this.processedAt = LocalDateTime.now();
    }
}
