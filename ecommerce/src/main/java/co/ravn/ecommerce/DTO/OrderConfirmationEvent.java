package co.ravn.ecommerce.DTO;

import co.ravn.ecommerce.Entities.Order.SaleOrder;

/**
 * Event published when an order payment succeeds and stock has been validated.
 * Used to trigger an order confirmation email.
 */
public record OrderConfirmationEvent(SaleOrder order) {
}

