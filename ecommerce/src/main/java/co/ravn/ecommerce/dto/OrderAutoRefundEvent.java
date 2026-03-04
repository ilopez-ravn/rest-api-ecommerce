package co.ravn.ecommerce.dto;

import co.ravn.ecommerce.entities.order.SaleOrder;

/**
 * Event published when a payment succeeds but is automatically refunded
 * due to insufficient stock at the time of fulfillment.
 */
public record OrderAutoRefundEvent(
        SaleOrder order,
        String refundReason
) {
}

