package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.ReturnShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReturnShipmentRepository extends JpaRepository<ReturnShipment, Integer> {
    Optional<ReturnShipment> findByRefundRequestId(int refundRequestId);
}
