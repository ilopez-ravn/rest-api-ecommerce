package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.RefundResponse;
import co.ravn.ecommerce.dto.response.order.ReturnShipmentResponse;
import co.ravn.ecommerce.entities.order.RefundRequest;
import co.ravn.ecommerce.entities.order.ReturnShipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefundMapper {

    @Mapping(source = "order.id", target = "order_id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "requiresReturn", target = "requires_return")
    @Mapping(source = "managerNotes", target = "manager_notes")
    @Mapping(source = "stripeRefundId", target = "stripe_refund_id")
    @Mapping(source = "refundAmount", target = "refund_amount")
    @Mapping(source = "requestedAt", target = "requested_at")
    @Mapping(source = "reviewedAt", target = "reviewed_at")
    @Mapping(source = "refundedAt", target = "refunded_at")
    @Mapping(source = "returnShipment", target = "return_shipment")
    RefundResponse toResponse(RefundRequest refundRequest);

    @Mapping(source = "trackingNumber", target = "tracking_number")
    @Mapping(source = "carrierName", target = "carrier_name")
    @Mapping(source = "shippedAt", target = "shipped_at")
    @Mapping(source = "receivedAt", target = "received_at")
    ReturnShipmentResponse toReturnShipmentResponse(ReturnShipment returnShipment);
}
