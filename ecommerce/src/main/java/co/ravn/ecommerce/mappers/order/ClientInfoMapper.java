package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.ClientInfoResponse;
import co.ravn.ecommerce.entities.auth.Person;
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
