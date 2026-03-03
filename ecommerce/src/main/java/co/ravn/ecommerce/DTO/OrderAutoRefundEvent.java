package co.ravn.ecommerce.DTO;

import co.ravn.ecommerce.Entities.Order.SaleOrder;

/**
 * Event published when a payment succeeds but is automatically refunded
 * due to insufficient stock at the time of fulfillment.
 */
public record OrderAutoRefundEvent(
        SaleOrder order,
        String refundReason
) {
}

