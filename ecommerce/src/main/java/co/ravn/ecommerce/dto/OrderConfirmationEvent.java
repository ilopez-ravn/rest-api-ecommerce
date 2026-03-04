package co.ravn.ecommerce.dto;

import co.ravn.ecommerce.entities.order.SaleOrder;

/**
 * Event published when an order payment succeeds and stock has been validated.
 * Used to trigger an order confirmation email.
 */
public record OrderConfirmationEvent(SaleOrder order) {
}

