package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.AddressResponse;
import co.ravn.ecommerce.Entities.Clients.ClientAddress;
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
