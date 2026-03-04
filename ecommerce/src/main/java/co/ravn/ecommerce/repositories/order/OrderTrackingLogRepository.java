package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.OrderTrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackingLogRepository extends JpaRepository<OrderTrackingLog, Integer> {
    List<OrderTrackingLog> findByDeliveryTrackingIdOrderByChangedAtDesc(int deliveryTrackingId);
}
