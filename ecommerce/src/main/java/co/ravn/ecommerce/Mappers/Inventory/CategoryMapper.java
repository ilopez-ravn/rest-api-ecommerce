package co.ravn.ecommerce.Mappers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.CategoryCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.CategoryResponse;
import co.ravn.ecommerce.Entities.Inventory.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "active", target = "is_active")
    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isActive", expression = "java(true)")
    Category toEntity(CategoryCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateFromRequest(CategoryUpdateRequest request, @MappingTarget Category category);
}
