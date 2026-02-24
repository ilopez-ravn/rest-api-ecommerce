package co.ravn.ecommerce.Mappers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.TagCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.TagUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.TagResponse;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(source = "isActive", target = "is_active")
    TagResponse toResponse(Tag tag);

    Tag toEntity(TagCreateRequest request);

    void updateFromRequest(TagUpdateRequest request, @MappingTarget Tag tag);
}
