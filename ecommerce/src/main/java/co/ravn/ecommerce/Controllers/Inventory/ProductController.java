package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.ProductFilterRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductCursorPage;
import co.ravn.ecommerce.Services.Inventory.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("")
    public ResponseEntity<?> getFilteredProducts(
            @ModelAttribute ProductFilterRequest productFilterRequest,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize,
            @RequestParam(value = "cursor", required = false) Integer cursor,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "sort_by", required = false) String sortBy,
            @RequestParam(value = "sort_order", required = false) String _sortOrder,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "categories_id", required = false) java.util.List<Integer> categoriesIds,
            @RequestParam(value = "tags_id", required = false) java.util.List<Integer> tagsId,
            @RequestParam(value = "min_price", required = false) java.math.BigDecimal minPrice,
            @RequestParam(value = "max_price", required = false) java.math.BigDecimal maxPrice,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "is_active", required = false) Boolean isActive
    ) {
        if (page != null) {
            productFilterRequest.setPage(page);
        }
        if (pageSize != null) {
            productFilterRequest.setPageSize(pageSize);
        }
        if (sortBy != null) {
            productFilterRequest.setSortBy(sortBy);
        }
        if (_sortOrder != null) {
            productFilterRequest.setSortOrder(_sortOrder);
        }
        if (filter != null) {
            productFilterRequest.setFilter(filter);
        }
        if (categoriesIds != null) {
            productFilterRequest.setCategoriesIds(categoriesIds);
        }
        if (tagsId != null) {
            productFilterRequest.setTagsId(tagsId);
        }
        if (minPrice != null) {
            productFilterRequest.setMinPrice(minPrice);
        }
        if (maxPrice != null) {
            productFilterRequest.setMaxPrice(maxPrice);
        }
        if (available != null) {
            productFilterRequest.setAvailable(available);
        }
        if (isActive != null) {
            productFilterRequest.setIsActive(isActive);
        }

        if (limit == null) {
            limit = productFilterRequest.getPageSize();
        }

        ProductCursorPage productPage = productService.getFilteredProductsCursor(productFilterRequest, cursor, limit);
        return ResponseEntity.ok().body(productPage);
    }


}
