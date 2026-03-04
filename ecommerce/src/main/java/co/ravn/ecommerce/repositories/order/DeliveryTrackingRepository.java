package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Integer> {
    Optional<DeliveryTracking> findByOrderId(int orderId);
    Optional<DeliveryTracking> findByTrackingNumber(String trackingNumber);
    List<DeliveryTracking> findByCarrierId(int carrierId);
    List<DeliveryTracking> findByStatusId(int statusId);
}
