package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.AddressResponse;
import co.ravn.ecommerce.entities.clients.ClientAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(source = "client.id", target = "client_id")
    @Mapping(source = "addressLine1", target = "address_line1")
    @Mapping(source = "addressLine2", target = "address_line2")
    @Mapping(source = "postalCode", target = "postal_code")
    @Mapping(source = "createdAt", target = "created_at")
    AddressResponse toResponse(ClientAddress address);
}
