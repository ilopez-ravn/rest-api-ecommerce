package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.ProductFilterRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductSpecification;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductCursorPage;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> getFilteredProducts(ProductFilterRequest productFilterRequest, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.withSearchCriteria(productFilterRequest), pageable);

    }

    public ProductCursorPage getFilteredProductsCursor(ProductFilterRequest productFilterRequest, Integer cursor, int limit) {
        int pageSize = Math.max(1, Math.min(limit, 100));
        Pageable pageable = PageRequest.of(0, pageSize + 1, Sort.by(Sort.Direction.ASC, productFilterRequest.getSortBy()));
        long totalItems = productRepository.count(ProductSpecification.withSearchCriteria(productFilterRequest));
        List<Product> products = productRepository
                .findAll(ProductSpecification.withSearchCriteria(productFilterRequest, cursor), pageable)
                .getContent();

        boolean hasMore = products.size() > pageSize;
        if (hasMore) {
            products = products.subList(0, pageSize);
        }

        Integer nextCursor = null;
        if (hasMore && !products.isEmpty()) {
            nextCursor = products.getLast().getId();
        }

        return new ProductCursorPage(products, nextCursor, hasMore, totalItems);
    }

}
