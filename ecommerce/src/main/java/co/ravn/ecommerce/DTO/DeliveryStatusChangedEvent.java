package co.ravn.ecommerce.DTO;

import co.ravn.ecommerce.Entities.Order.DeliveryStatus;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;

/**
 * Event published when an order's delivery status changes.
 * Used to trigger delivery status update emails asynchronously.
 */
public record DeliveryStatusChangedEvent(
        DeliveryTracking tracking,
        DeliveryStatus previousStatus,
        DeliveryStatus newStatus
) {
}

