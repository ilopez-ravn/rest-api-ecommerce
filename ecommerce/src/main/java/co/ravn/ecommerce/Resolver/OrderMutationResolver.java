package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.Services.Order.OrderService;
import lombok.AllArgsConstructor;
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
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public Boolean deleteOrder(@Argument int id) {
        orderService.deleteOrder(id);
        return true;
    }
}

