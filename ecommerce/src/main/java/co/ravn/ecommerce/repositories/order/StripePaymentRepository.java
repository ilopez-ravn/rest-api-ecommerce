package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.StripePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripePaymentRepository extends JpaRepository<StripePayment, Integer> {
    Optional<StripePayment> findByOrderId(int orderId);
    Optional<StripePayment> findByStripePaymentId(String stripePaymentId);
}
