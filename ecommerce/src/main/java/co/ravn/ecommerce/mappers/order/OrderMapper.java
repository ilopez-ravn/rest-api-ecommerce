package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.*;
import co.ravn.ecommerce.entities.clients.ClientAddress;
import co.ravn.ecommerce.entities.order.DeliveryTracking;
import co.ravn.ecommerce.entities.order.OrderBill;
import co.ravn.ecommerce.entities.order.OrderDetails;
import co.ravn.ecommerce.entities.order.SaleOrder;
import co.ravn.ecommerce.entities.order.StripePayment;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        OrderItemMapper.class,
        BillingInfoMapper.class,
        AddressMapper.class,
        ClientInfoMapper.class,
        WarehouseInfoMapper.class,
        StripePaymentMapper.class,
        DeliveryTrackingMapper.class,
        TrackingLogMapper.class
})
public interface OrderMapper {

    @Mapping(source = "order.id", target = "id")
    @Mapping(source = "order.orderDate", target = "order_date")
    @Mapping(source = "bill", target = "bill")
    @Mapping(source = "payment", target = "payment")
    @Mapping(source = "order.client", target = "client")
    @Mapping(source = "order.warehouse", target = "warehouse")
    OrderResponse toResponse(SaleOrder order, OrderBill bill, StripePayment payment);

    @Mapping(source = "order.id", target = "id")
    @Mapping(source = "order.orderDate", target = "order_date")
    @Mapping(source = "bill", target = "bill")
    @Mapping(source = "payment", target = "payment")
    @Mapping(source = "order.client", target = "client")
    @Mapping(source = "order.warehouse", target = "warehouse")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "tracking", target = "tracking")
    @Mapping(source = "orderDetails", target = "items")
    OrderResponse toResponse(SaleOrder order, OrderBill bill, StripePayment payment, ClientAddress address, DeliveryTracking tracking, List<OrderDetails> orderDetails);
}
