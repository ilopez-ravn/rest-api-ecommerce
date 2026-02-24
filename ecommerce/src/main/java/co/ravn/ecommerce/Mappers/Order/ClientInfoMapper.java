package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.ClientInfoResponse;
import co.ravn.ecommerce.Entities.Auth.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientInfoMapper {

    @Mapping(source = "firstName", target = "first_name")
    @Mapping(source = "lastName", target = "last_name")
    @Mapping(source = "documentType", target = "document_type")
    @Mapping(source = "createdAt", target = "created_at")
    ClientInfoResponse toResponse(Person client);
}
