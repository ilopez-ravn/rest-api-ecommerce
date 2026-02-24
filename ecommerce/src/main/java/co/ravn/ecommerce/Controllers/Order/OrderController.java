package co.ravn.ecommerce.Controllers.Order;

import co.ravn.ecommerce.DTO.Request.Order.NewOrderRequest;
import co.ravn.ecommerce.DTO.Request.Order.ShippingStatusUpdateRequest;
import co.ravn.ecommerce.Services.Order.OrderService;
import co.ravn.ecommerce.Services.Payments.StripePaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/orders")
@AllArgsConstructor
@Validated
public class OrderController {

    private final StripePaymentService stripePaymentService;
    private final OrderService orderService;

    @GetMapping("/status/{shoppingCartId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable @Min(1) int shoppingCartId) {
        return stripePaymentService.getOrderStatusByShoppingCartId(shoppingCartId);
    }

    @GetMapping("")
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) Integer client_id,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size,
            @RequestParam(required = false) String sort_by,
            @RequestParam(defaultValue = "desc") String sort_order,
            @RequestParam(required = false) String date_from,
            @RequestParam(required = false) String date_to
    ) {
        return orderService.getOrders(client_id, status, page, page_size, sort_by, sort_order, date_from, date_to);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable @Min(1) int orderId) {
        return orderService.getOrderById(orderId);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable @Min(1) int orderId) {
        return orderService.deleteOrder(orderId);
    }

    @GetMapping("/{orderId}/shipping")
    public ResponseEntity<?> getShippingDetails(@PathVariable @Min(1) int orderId) {
        return orderService.getShippingDetails(orderId);
    }

    @PostMapping("/{orderId}/shipping")
    public ResponseEntity<?> updateShippingStatus(
            @PathVariable @Min(1) int orderId,
            @Valid @RequestBody ShippingStatusUpdateRequest request
    ) {
        return orderService.updateShippingStatus(orderId, request);
    }
}
