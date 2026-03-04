package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.StripePaymentResponse;
import co.ravn.ecommerce.entities.order.StripePayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StripePaymentMapper {

    @Mapping(source = "order.id", target = "order_id")
    @Mapping(target = "payment_type", expression = "java(\"stripe\")")
    @Mapping(source = "paymentStatus", target = "status")
    @Mapping(source = "paymentMethod", target = "payment_method")
    StripePaymentResponse toResponse(StripePayment payment);
}

