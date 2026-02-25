package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.Request.Order.NewOrderRequest;
import co.ravn.ecommerce.DTO.Response.Order.OrderResponse;
import co.ravn.ecommerce.Services.Order.OrderService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@AllArgsConstructor
public class OrderMutationResolver {

    private final OrderService orderService;

    @MutationMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    @Transactional
    public OrderResponse createOrderByShoppingCart(@Argument("newOrder") NewOrderInput input) {
        NewOrderRequest request = new NewOrderRequest(
                input.getWarehouseId(),
                input.getCartId(),
                input.getAddressId()
        );
        return orderService.createOrder(request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public Boolean deleteOrder(@Argument int id) {
        orderService.deleteOrder(id);
        return true;
    }

    @Setter
    @Getter
    public static class NewOrderInput {
        private Integer cartId;
        private Integer warehouseId;
        private Integer addressId;

    }
}

