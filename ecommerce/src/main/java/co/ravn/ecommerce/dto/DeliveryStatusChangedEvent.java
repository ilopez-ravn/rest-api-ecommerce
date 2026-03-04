package co.ravn.ecommerce.dto;

import co.ravn.ecommerce.entities.order.DeliveryStatus;
import co.ravn.ecommerce.entities.order.DeliveryTracking;

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

