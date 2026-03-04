package co.ravn.ecommerce.mappers.inventory;

import co.ravn.ecommerce.dto.response.inventory.ProductImageResponse;
import co.ravn.ecommerce.entities.inventory.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    @Mapping(source = "imageUrl", target = "image_url")
    @Mapping(source = "productId", target = "product_id")
    @Mapping(source = "isPrimaryImage", target = "is_primary_image")
    @Mapping(source = "publicId", target = "public_id")
    @Mapping(source = "isActive", target = "is_active")
    @Mapping(source = "createdAt", target = "created_at")
    ProductImageResponse toResponse(ProductImage image);
}
