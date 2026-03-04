package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.graphql.NewProductInput;
import co.ravn.ecommerce.dto.graphql.ProductImageInput;
import co.ravn.ecommerce.dto.graphql.UpdateProductInput;
import co.ravn.ecommerce.dto.request.inventory.ProductImageUpdate;
import co.ravn.ecommerce.dto.request.inventory.ProductUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.ProductResponse;
import co.ravn.ecommerce.dto.response.MessageResponse;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import co.ravn.ecommerce.services.inventory.ProductService;
import lombok.AllArgsConstructor;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class ProductMutationResolver {

    private final ProductService productService;
    private final ProductRepository productRepository;


    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public Product createProduct(@Argument("newProduct") NewProductInput newProduct) {
        ProductUpdateRequest request = toProductUpdateRequest(newProduct);
        ProductResponse response = productService.createProduct(request);
        return productRepository.findByIdAndDeletedAtIsNull(response.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + response.getId()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public Product updateProduct(@Argument("updatedProduct") UpdateProductInput updatedProduct) {
        if (updatedProduct.getId() == null) {
            throw new IllegalArgumentException("Product id is required");
        }
        ProductUpdateRequest request = toProductUpdateRequest(updatedProduct);
        ProductResponse response = productService.updateProduct(updatedProduct.getId(), request);
        return productRepository.findByIdAndDeletedAtIsNull(response.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + response.getId()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public Boolean deleteProduct(@Argument int id) {
        productService.deleteProduct(id);
        return true;
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public Product updateProductStatus(@Argument int id, @Argument boolean status) {
        return productService.updateProductStatus(id, status);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT','MANAGER')")
    @Transactional
    public Boolean likeAProduct(@Argument int productId) {
        MessageResponse response = productService.updateProductLiked(productId);
        return response.getMessage() != null && response.getMessage().toLowerCase().contains("successfully");
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT','MANAGER')")
    @Transactional
    public Boolean removeLikeFromProduct(@Argument int productId) {
        MessageResponse response = productService.deleteProductLiked(productId);
        return response.getMessage() != null && response.getMessage().toLowerCase().contains("successfully");
    }

    private ProductUpdateRequest toProductUpdateRequest(NewProductInput input) {
        BigDecimal price = input.getPrice() != null ? BigDecimal.valueOf(input.getPrice()) : null;
        List<ProductImageUpdate> images = toProductImageUpdates(input.getImageList());
        // New products are active by default
        return new ProductUpdateRequest(
                input.getName(),
                input.getDescription(),
                price,
                input.getCategoryList(),
                input.getTagList(),
                images,
                Boolean.TRUE
        );
    }

    private ProductUpdateRequest toProductUpdateRequest(UpdateProductInput input) {
        BigDecimal price = input.getPrice() != null ? BigDecimal.valueOf(input.getPrice()) : null;
        List<ProductImageUpdate> images = toProductImageUpdates(input.getImageList());
        // isActive is controlled via updateProductStatus mutation
        return new ProductUpdateRequest(
                input.getName(),
                input.getDescription(),
                price,
                input.getCategoryList(),
                input.getTagList(),
                images,
                null
        );
    }

    private List<ProductImageUpdate> toProductImageUpdates(List<ProductImageInput> imageInputs) {
        if (imageInputs == null || imageInputs.isEmpty()) {
            return null;
        }
        List<ProductImageUpdate> images = new ArrayList<>();
        for (ProductImageInput input : imageInputs) {
            ProductImageUpdate imageUpdate = new ProductImageUpdate();
            imageUpdate.setImageUrl(input.getImageUrl());
            imageUpdate.setIsPrimaryImage(input.getIsPrimary());
            // Reuse image URL as a simple stable public id when coming from GraphQL
            imageUpdate.setPublicId(input.getImageUrl());
            images.add(imageUpdate);
        }
        return images;
    }
}

