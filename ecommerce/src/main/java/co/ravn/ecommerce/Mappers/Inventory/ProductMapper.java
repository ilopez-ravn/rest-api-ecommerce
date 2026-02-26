package co.ravn.ecommerce.Mappers.Inventory;

import co.ravn.ecommerce.DTO.Response.Inventory.ProductResponse;
import co.ravn.ecommerce.Entities.Inventory.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, TagMapper.class, ProductImageMapper.class})
public interface ProductMapper {

    @Mapping(source = "isActive", target = "is_active")
    @Mapping(source = "productImages", target = "product_images")
    @Mapping(source = "createdAt", target = "created_at")
    @Mapping(target = "stock", ignore = true)
    ProductResponse toResponse(Product product);

    @AfterMapping
    default void handleNullCollections(@MappingTarget ProductResponse response) {
        if (response.getCategories() == null) response.setCategories(Collections.emptyList());
        if (response.getTags() == null) response.setTags(Collections.emptyList());
        if (response.getProduct_images() == null) response.setProduct_images(Collections.emptyList());
    }
}
