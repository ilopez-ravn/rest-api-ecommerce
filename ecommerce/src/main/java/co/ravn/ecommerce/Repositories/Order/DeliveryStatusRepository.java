package co.ravn.ecommerce.Repositories.Order;

import co.ravn.ecommerce.Entities.Order.DeliveryStatus;
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
