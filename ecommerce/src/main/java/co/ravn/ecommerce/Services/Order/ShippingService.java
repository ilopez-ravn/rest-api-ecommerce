package co.ravn.ecommerce.Services.Order;

import co.ravn.ecommerce.Entities.Clients.ClientAddress;
import co.ravn.ecommerce.Entities.Order.DeliveryStatus;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;
import co.ravn.ecommerce.Entities.Order.OrderTrackingLog;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Clients.ClientAddressRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryStatusRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryTrackingRepository;
import co.ravn.ecommerce.Repositories.Order.OrderTrackingLogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class ShippingService {

    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final OrderTrackingLogRepository orderTrackingLogRepository;
    private final ClientAddressRepository clientAddressRepository;

    public DeliveryTracking createInitialTracking(SaleOrder order, int addressId) {
        ClientAddress address = clientAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        List<DeliveryStatus> statuses = deliveryStatusRepository.findAllByOrderByStepOrder();
        if (statuses.isEmpty()) {
            throw new RuntimeException("No delivery statuses configured");
        }

        DeliveryStatus initialStatus = statuses.get(0);
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        DeliveryTracking tracking = deliveryTrackingRepository.save(
                new DeliveryTracking(order, address, null, null, initialStatus, trackingNumber, null)
        );

        orderTrackingLogRepository.save(
                new OrderTrackingLog(tracking, null, initialStatus, null)
        );

        log.info("Created initial delivery tracking for order id={} with tracking number={}",
                order.getId(), trackingNumber);

        return tracking;
    }
}

