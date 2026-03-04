package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.GraphQL.OrderPage;
import co.ravn.ecommerce.DTO.Response.Order.OrderResponse;
import co.ravn.ecommerce.DTO.Response.Order.OrderStatusResponse;
import co.ravn.ecommerce.DTO.Response.Order.PaginatedOrderResponse;
import co.ravn.ecommerce.Services.Order.OrderService;
import co.ravn.ecommerce.Services.Payments.StripePaymentService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class OrderQueryResolver {

    private final OrderService orderService;
    private final StripePaymentService stripePaymentService;


    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public OrderPage orders(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String clientId,
            @Argument String status,
            @Argument String sortBy,
            @Argument String sortOrder,
            @Argument String dateFrom,
            @Argument String dateTo
    ) {
        int pageNum = (page != null && page > 0) ? page : 1;
        int pageSize = (size != null && size > 0) ? size : 10;
        String order = (sortOrder != null && !sortOrder.isBlank()) ? sortOrder : "desc";
        Integer clientIdInt = parseId(clientId);
        PaginatedOrderResponse response = orderService.getOrders(
                clientIdInt,
                status,
                pageNum,
                pageSize,
                sortBy,
                order,
                dateFrom,
                dateTo
        );
        return new OrderPage(response);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public OrderPage myOrders(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String status,
            @Argument String sortBy,
            @Argument String sortOrder,
            @Argument String dateFrom,
            @Argument String dateTo
    ) {
        int pageNum = (page != null && page > 0) ? page : 1;
        int pageSize = (size != null && size > 0) ? size : 10;
        String order = (sortOrder != null && !sortOrder.isBlank()) ? sortOrder : "desc";
        PaginatedOrderResponse response = orderService.getOrders(
                null,
                status,
                pageNum,
                pageSize,
                sortBy,
                order,
                dateFrom,
                dateTo
        );
        return new OrderPage(response);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public OrderResponse getOrderById(@Argument int id) {
        return orderService.getOrderById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public OrderStatusResponse getOrderStatus(@Argument int shoppingCartId) {
        return stripePaymentService.getOrderStatusByShoppingCartId(shoppingCartId);
    }

    /** Parse GraphQL ID (often sent as string) to Integer; null if absent or blank. */
    private static Integer parseId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

