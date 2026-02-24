package co.ravn.ecommerce.Repositories.Order;

import co.ravn.ecommerce.Entities.Order.OrderTrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackingLogRepository extends JpaRepository<OrderTrackingLog, Integer> {
    List<OrderTrackingLog> findByDeliveryTrackingIdOrderByChangedAtDesc(int deliveryTrackingId);
}
