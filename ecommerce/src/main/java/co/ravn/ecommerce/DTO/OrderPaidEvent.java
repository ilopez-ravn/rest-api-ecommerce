package co.ravn.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event emitted when an order's payment succeeds (Stripe webhook payment_intent.succeeded).
 * Used to push to GraphQL subscription checkOrderPaymentStatus(shoppingCartId).
 */
@Getter
@AllArgsConstructor
public class OrderPaidEvent {
    private final int shoppingCartId;
    private final int orderId;
}
