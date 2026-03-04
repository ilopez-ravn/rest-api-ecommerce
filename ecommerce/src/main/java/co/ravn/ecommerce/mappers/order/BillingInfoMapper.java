package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.BillingInfoResponse;
import co.ravn.ecommerce.entities.order.OrderBill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillingInfoMapper {

    @Mapping(source = "documentNumber", target = "document_number")
    @Mapping(source = "documentType", target = "document_type")
    @Mapping(source = "totalAmount", target = "total_amount")
    @Mapping(source = "taxAmount", target = "tax_amount")
    @Mapping(source = "deliveryFee", target = "delivery_fee")
    BillingInfoResponse toResponse(OrderBill bill);
}
