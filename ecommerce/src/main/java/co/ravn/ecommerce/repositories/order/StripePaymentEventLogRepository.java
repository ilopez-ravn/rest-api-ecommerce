package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.StripePaymentEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StripePaymentEventLogRepository extends JpaRepository<StripePaymentEventLog, Integer> {
    List<StripePaymentEventLog> findByPaymentId(int paymentId);
    Optional<StripePaymentEventLog> findByPaymentIdAndEventType(int paymentId, String eventType);
}
