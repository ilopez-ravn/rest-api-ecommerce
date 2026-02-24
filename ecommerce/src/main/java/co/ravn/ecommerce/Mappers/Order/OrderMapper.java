package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.*;
import co.ravn.ecommerce.Entities.Clients.ClientAddress;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;
import co.ravn.ecommerce.Entities.Order.OrderBill;
import co.ravn.ecommerce.Entities.Order.OrderDetails;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Entities.Order.StripePayment;

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
