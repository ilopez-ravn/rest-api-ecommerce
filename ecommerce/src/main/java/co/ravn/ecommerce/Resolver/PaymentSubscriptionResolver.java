package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.OrderPaidEvent;
import co.ravn.ecommerce.DTO.Response.Order.OrderResponse;
import co.ravn.ecommerce.Services.Order.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@AllArgsConstructor
public class PaymentSubscriptionResolver {

    private final reactor.core.publisher.Sinks.Many<OrderPaidEvent> orderPaidSink;
    private final OrderService orderService;

    @SubscriptionMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    public Flux<OrderResponse> checkOrderPaymentStatus(@Argument int shoppingCartId) {
        return orderPaidSink.asFlux()
                .filter(event -> event.getShoppingCartId() == shoppingCartId)
                .map(event -> orderService.getOrderById(event.getOrderId()));
    }
}
