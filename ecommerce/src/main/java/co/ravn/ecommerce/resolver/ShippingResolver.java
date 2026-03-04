package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.request.order.ShippingStatusUpdateRequest;
import co.ravn.ecommerce.dto.response.order.DeliveryStatusResponse;
import co.ravn.ecommerce.dto.response.order.ShippingDetailsResponse;
import co.ravn.ecommerce.services.order.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@AllArgsConstructor
public class ShippingResolver {

    private final OrderService orderService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT','WAREHOUSE','SHIPPING')")
    public ShippingDetailsResponse getOrderShippingDetails(@Argument int orderId) {
        return orderService.getShippingDetails(orderId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','WAREHOUSE','SHIPPING')")
    public List<DeliveryStatusResponse> getDeliveryStatuses() {
        return orderService.getDeliveryStatuses();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MANAGER','WAREHOUSE','SHIPPING')")
    @Transactional
    public ShippingDetailsResponse updateShippingStatus(@Argument int orderId, @Argument int statusId) {
        ShippingStatusUpdateRequest req = new ShippingStatusUpdateRequest(statusId);
        return orderService.updateShippingStatus(orderId, req);
    }
}

