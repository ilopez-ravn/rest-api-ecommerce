package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryStatusRepository extends JpaRepository<DeliveryStatus, Integer> {
    List<DeliveryStatus> findAllByOrderByStepOrder();
    Optional<DeliveryStatus> findByName(String name);
    Optional<DeliveryStatus> findFirstByOrderByStepOrder();
}
