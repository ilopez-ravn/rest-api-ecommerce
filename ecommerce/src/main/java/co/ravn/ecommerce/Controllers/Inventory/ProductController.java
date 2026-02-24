package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.ProductFilterRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductCursorPage;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductImageResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductResponse;
import co.ravn.ecommerce.DTO.Response.MessageResponse;
import co.ravn.ecommerce.Services.Inventory.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("")
    public ResponseEntity<ProductCursorPage> getFilteredProducts(
            @ModelAttribute ProductFilterRequest productFilterRequest,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize,
            @RequestParam(value = "cursor", required = false) Integer cursor,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "sort_by", required = false) String sortBy,
            @RequestParam(value = "sort_order", required = false) String sortOrder,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "categories_id", required = false) List<Integer> categoriesIds,
            @RequestParam(value = "tags_id", required = false) List<Integer> tagsId,
            @RequestParam(value = "min_price", required = false) BigDecimal minPrice,
            @RequestParam(value = "max_price", required = false) BigDecimal maxPrice,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "is_active", required = false) Boolean isActive
    ) {
        if (page != null) productFilterRequest.setPage(page);
        if (pageSize != null) productFilterRequest.setPageSize(pageSize);
        if (sortBy != null) productFilterRequest.setSortBy(sortBy);
        if (sortOrder != null) productFilterRequest.setSortOrder(sortOrder);
        if (filter != null) productFilterRequest.setFilter(filter);
        if (categoriesIds != null) productFilterRequest.setCategoriesIds(categoriesIds);
        if (tagsId != null) productFilterRequest.setTagsId(tagsId);
        if (minPrice != null) productFilterRequest.setMinPrice(minPrice);
        if (maxPrice != null) productFilterRequest.setMaxPrice(maxPrice);
        if (available != null) productFilterRequest.setAvailable(available);
        if (isActive != null) productFilterRequest.setIsActive(isActive);

        if (limit == null) {
            limit = productFilterRequest.getPageSize();
        }

        return ResponseEntity.ok(productService.getFilteredProductsCursor(productFilterRequest, cursor, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable @Min(1) int id, @RequestBody @Valid ProductUpdateRequest productUpdateRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, productUpdateRequest));
    }

    @PostMapping("")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductUpdateRequest productUpdateRequest) {
        return ResponseEntity.ok(productService.createProduct(productUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @Min(1) int id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/liked")
    public ResponseEntity<MessageResponse> updateProductLiked(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(productService.updateProductLiked(id));
    }

    @DeleteMapping("/{id}/liked")
    public ResponseEntity<MessageResponse> deleteProductLiked(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(productService.deleteProductLiked(id));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<List<ProductImageResponse>> uploadProductImages(
            @PathVariable @Min(1) int id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "is_primary_image", required = false) Boolean isPrimaryImage
    ) {
        return ResponseEntity.ok(productService.addProductImages(id, files, isPrimaryImage));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable @Min(1) int productId,
            @PathVariable @Min(1) int imageId
    ) {
        productService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

}
