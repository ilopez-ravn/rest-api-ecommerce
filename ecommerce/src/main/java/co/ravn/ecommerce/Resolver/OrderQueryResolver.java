package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.GraphQL.OrderPage;
import co.ravn.ecommerce.DTO.Response.Order.OrderResponse;
import co.ravn.ecommerce.DTO.Response.Order.PaginatedOrderResponse;
import co.ravn.ecommerce.Services.Order.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class OrderQueryResolver {

    private final OrderService orderService;


    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public OrderPage orders(@Argument int page, @Argument int size) {
        PaginatedOrderResponse response = orderService.getOrders(
                null,
                null,
                page,
                size,
                null,
                "desc",
                null,
                null
        );
        return new OrderPage(response);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public OrderPage myOrders(@Argument int page, @Argument int size) {
        PaginatedOrderResponse response = orderService.getOrders(
                null,
                null,
                page,
                size,
                null,
                "desc",
                null,
                null
        );
        return new OrderPage(response);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public OrderResponse getOrderById(@Argument int id) {
        return orderService.getOrderById(id);
    }
}

