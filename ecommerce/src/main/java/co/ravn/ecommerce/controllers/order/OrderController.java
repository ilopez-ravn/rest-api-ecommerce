package co.ravn.ecommerce.controllers.order;

import co.ravn.ecommerce.dto.request.order.ShippingStatusUpdateRequest;
import co.ravn.ecommerce.dto.response.order.DeliveryStatusResponse;
import co.ravn.ecommerce.dto.response.order.OrderResponse;
import co.ravn.ecommerce.dto.response.order.OrderStatusResponse;
import co.ravn.ecommerce.dto.response.order.PaginatedOrderResponse;
import co.ravn.ecommerce.dto.response.order.ShippingDetailsResponse;
import co.ravn.ecommerce.services.order.OrderService;
import co.ravn.ecommerce.services.payments.StripePaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@AllArgsConstructor
@Validated
public class OrderController {

    private final StripePaymentService stripePaymentService;
    private final OrderService orderService;

    @GetMapping("/status/{shoppingCartId}")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable @Min(1) int shoppingCartId) {
        return ResponseEntity.ok(stripePaymentService.getOrderStatusByShoppingCartId(shoppingCartId));
    }

    @GetMapping("")
    public ResponseEntity<PaginatedOrderResponse> getOrders(
            @RequestParam(required = false) Integer client_id,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size,
            @RequestParam(required = false) String sort_by,
            @RequestParam(defaultValue = "desc") String sort_order,
            @RequestParam(required = false) String date_from,
            @RequestParam(required = false) String date_to
    ) {
        return ResponseEntity.ok(orderService.getOrders(client_id, status, page, page_size, sort_by, sort_order, date_from, date_to));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable @Min(1) int orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable @Min(1) int orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}/shipping")
    public ResponseEntity<ShippingDetailsResponse> getShippingDetails(@PathVariable @Min(1) int orderId) {
        return ResponseEntity.ok(orderService.getShippingDetails(orderId));
    }

    @PostMapping("/{orderId}/shipping")
    public ResponseEntity<ShippingDetailsResponse> updateShippingStatus(
            @PathVariable @Min(1) int orderId,
            @Valid @RequestBody ShippingStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(orderService.updateShippingStatus(orderId, request));
    }

    @GetMapping("/delivery-status")
    public ResponseEntity<List<DeliveryStatusResponse>> getDeliveryStatuses() {
        return ResponseEntity.ok(orderService.getDeliveryStatuses());
    }
}
