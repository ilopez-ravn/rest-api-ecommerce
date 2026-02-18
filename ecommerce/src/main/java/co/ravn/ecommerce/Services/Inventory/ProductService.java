package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.ProductFilterRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductSpecification;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductUpdateRequest;
import co.ravn.ecommerce.DTO.Response.MessageResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductCursorPage;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductResponse;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Cart.ProductLiked;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductChangesLog;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Cart.ProductLikedRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.TagRepository;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductChangesLogRepository productChangesLogRepository;
    private final TagRepository tagRepository;
    private final ProductLikedRepository productLikedRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(UserRepository userRepository, ProductRepository productRepository, ProductChangesLogRepository productChangesLogRepository, TagRepository tagRepository, ProductLikedRepository productLikedRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productChangesLogRepository = productChangesLogRepository;
        this.tagRepository = tagRepository;
        this.productLikedRepository = productLikedRepository;
        this.categoryRepository = categoryRepository;
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

    @Transactional
    public ResponseEntity<?> updateProduct(int id, ProductUpdateRequest productUpdateRequest) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Log changes
        List<String> changes = new ArrayList<>();

        if (productUpdateRequest.getName() != null && !productUpdateRequest.getName().equals(product.getName())) {
            product.setName(productUpdateRequest.getName());
            changes.add("Name changed from '" + product.getName() + "' to '" + productUpdateRequest.getName() + "'");
        }
        if (productUpdateRequest.getDescription() != null && !productUpdateRequest.getDescription().equals(product.getDescription())) {
            product.setDescription(productUpdateRequest.getDescription());
            changes.add("Description changed from '" + product.getDescription() + "' to '" + productUpdateRequest.getDescription() + "'");
        }
        if (productUpdateRequest.getPrice() != null && !productUpdateRequest.getPrice().equals(product.getPrice())) {
            product.setPrice(productUpdateRequest.getPrice());
            changes.add("Price changed from '" + product.getPrice() + "' to '" + productUpdateRequest.getPrice() + "'");
        }
        if (productUpdateRequest.getIsActive() != null && !productUpdateRequest.getIsActive().equals(product.getIsActive())) {
            product.setIsActive(productUpdateRequest.getIsActive());
            changes.add("Active status changed from '" + product.getIsActive() + "' to '" + productUpdateRequest.getIsActive() + "'");
        }

        if (productUpdateRequest.getTagList() != null) {
            if (product.getTags() == null) {
                product.setTags(new ArrayList<>());
            }

            // get tags ids that don't exist in the request and remove them
            List<Tag> tagsToRemove = product.getTags().stream()
                    .filter(tag -> !productUpdateRequest.getTagList().contains(tag.getId()))
                    .toList();
            
            product.getTags().removeAll(tagsToRemove);
            if (!productUpdateRequest.getTagList().isEmpty()) {
                // get ids for new tags and add them
                List<Integer> newTagIds = productUpdateRequest.getTagList().stream()
                        .filter(tagId -> product.getTags().stream().noneMatch(t -> t.getId() == tagId))
                        .toList();
                List<Tag> tags = tagRepository.findAllByIdInAndIsActiveTrue(newTagIds);
                product.getTags().addAll(tags);
            } else {
                changes.add("Tags cleared");
            }
        }
        if (productUpdateRequest.getCategoryList() != null) {
            if (product.getCategories() == null) {
                product.setCategories(new ArrayList<>());
            }

            // get categories ids that don't exist in the request and remove them
            List<Category> categoriesToRemove = product.getCategories().stream()
                    .filter(category -> !productUpdateRequest.getCategoryList().contains(category.getId()))
                    .toList();

            product.getCategories().removeAll(categoriesToRemove);

            if (!productUpdateRequest.getCategoryList().isEmpty()) {
                // get ids for new categories and add them
                List<Integer> newCategoryIds = productUpdateRequest.getCategoryList().stream()
                        .filter(categoryId -> product.getCategories().stream().noneMatch(c -> c.getId() == categoryId))
                        .toList();
                List<Category> categories = categoryRepository.findAllByIdInAndIsActiveTrue(newCategoryIds);
                product.getCategories().addAll(categories);
                changes.add("Categories updated to: " + productUpdateRequest.getCategoryList());
            } else {
                changes.add("Categories cleared");
            }
        }

        if (!changes.isEmpty()) {
            // Load logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + auth.getName()));

            String changeDescription = String.join("; ", changes);
            ProductChangesLog changeLog = new ProductChangesLog(
                            product,
                            changeDescription,
                            loggedInUser
                    );
            productChangesLogRepository.save(changeLog);
        }

        productRepository.save(product);
        
        return ResponseEntity.ok(new ProductResponse(product));
    }

    @Transactional
    public ResponseEntity<?> createProduct(ProductUpdateRequest productUpdateRequest) {
        Product product = new Product();
        product.setName(productUpdateRequest.getName());
        product.setDescription(productUpdateRequest.getDescription());
        product.setPrice(productUpdateRequest.getPrice());
        product.setIsActive(productUpdateRequest.getIsActive());
        
        // Set tags if provided
        if (productUpdateRequest.getTagList() != null && !productUpdateRequest.getTagList().isEmpty()) {
            List<Tag> tags = tagRepository.findAllByIdInAndIsActiveTrue(productUpdateRequest.getTagList());
            product.setTags(tags);
        }
        
        // Set categories if provided
        if (productUpdateRequest.getCategoryList() != null && !productUpdateRequest.getCategoryList().isEmpty()) {
            List<Category> categories = categoryRepository.findAllByIdInAndIsActiveTrue(productUpdateRequest.getCategoryList());
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(new ProductResponse(savedProduct));
    }

    @Transactional
    public ResponseEntity<?> deleteProduct(int id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<?> updateProductLiked(int id) {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + auth.getName()));


        Optional<ProductLiked> productLiked = productLikedRepository.findByProductIdAndUserId(id, loggedInUser.getId());

        if(productLiked.isPresent()) {
            return ResponseEntity.ok().body(
                new MessageResponse("Product already liked by this user")
            );
        }

        ProductLiked newProductLiked = new ProductLiked(
            loggedInUser,
            productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)),
            false
        );

        productLikedRepository.save(newProductLiked);
        return ResponseEntity.ok().body(
            new MessageResponse("Product liked successfully")
        );
    }

    @Transactional
    public ResponseEntity<?> deleteProductLiked(int id) {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + auth.getName()));


        Optional<ProductLiked> productLiked = productLikedRepository.findByProductIdAndUserId(id, loggedInUser.getId());

        if(productLiked.isEmpty()) {
            return ResponseEntity.ok().body(
                new MessageResponse("Product not liked by this user")
            );
        }

        productLikedRepository.delete(productLiked.get());
        return ResponseEntity.ok().body(
            new MessageResponse("Product unliked successfully")
        );
    }
}
