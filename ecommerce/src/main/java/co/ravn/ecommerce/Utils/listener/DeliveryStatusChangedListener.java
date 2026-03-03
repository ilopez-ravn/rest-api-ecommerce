package co.ravn.ecommerce.Utils.listener;

import co.ravn.ecommerce.DTO.DeliveryStatusChangedEvent;
import co.ravn.ecommerce.Services.Order.DeliveryStatusEmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for delivery status changes and delegates email sending
 * to DeliveryStatusEmailService asynchronously.
 */
@Component
@AllArgsConstructor
@Slf4j
public class DeliveryStatusChangedListener {

    private final DeliveryStatusEmailService emailService;

    @Async
    @EventListener
    public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        try {
            emailService.sendDeliveryStatusUpdateEmail(
                    event.tracking(),
                    event.previousStatus(),
                    event.newStatus()
            );
        } catch (Exception e) {
            // Errors are already logged inside DeliveryStatusEmailService; we only add context here.
            log.error("Failed to process DeliveryStatusChangedEvent for tracking id={}",
                    event.tracking() != null ? event.tracking().getId() : null, e);
        }
    }
}

