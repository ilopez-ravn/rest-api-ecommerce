package co.ravn.ecommerce.mappers.inventory;

import co.ravn.ecommerce.dto.request.inventory.TagCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.TagUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.TagResponse;
import co.ravn.ecommerce.entities.inventory.Tag;
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
