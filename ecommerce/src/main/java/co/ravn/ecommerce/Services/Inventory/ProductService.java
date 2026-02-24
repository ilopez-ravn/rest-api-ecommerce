package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.ProductFilterRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductSpecification;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductUpdateRequest;
import co.ravn.ecommerce.DTO.Response.MessageResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ImageUploadResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductCursorPage;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductImageResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductStockResponse;
import co.ravn.ecommerce.DTO.ProductPriceDropEvent;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Cart.ProductLiked;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductChangesLog;
import co.ravn.ecommerce.Entities.Inventory.ProductImage;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductImageUpdate;
import co.ravn.ecommerce.Mappers.Inventory.ProductImageMapper;
import co.ravn.ecommerce.Mappers.Inventory.ProductMapper;
import co.ravn.ecommerce.Mappers.Inventory.ProductStockMapper;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Cart.ProductLikedRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductImageRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.TagRepository;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import org.springframework.context.ApplicationEventPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
@AllArgsConstructor
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductChangesLogRepository productChangesLogRepository;
    private final TagRepository tagRepository;
    private final ProductLikedRepository productLikedRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductImageRepository productImageRepository;
    private final ProductImageMapper productImageMapper;
    private final ProductStockMapper productStockMapper;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher applicationEventPublisher;

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

        List<ProductResponse> productResponses = products.stream()
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    // attach total stock across all warehouses using JPA relationship
                    List<ProductStock> stocks = product.getStock();
                    if (stocks != null && !stocks.isEmpty()) {
                        int totalQuantity = stocks.stream()
                                .mapToInt(ProductStock::getQuantity)
                                .sum();
                        ProductStock firstStock = stocks.getFirst();
                        ProductStockResponse stockResponse = productStockMapper.toResponse(firstStock);
                        stockResponse.setQuantity(totalQuantity);
                        response.setStock(stockResponse);
                    }
                    return response;
                })
                .toList();
        return new ProductCursorPage(productResponses, nextCursor, hasMore, totalItems);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(int id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        ProductResponse response = productMapper.toResponse(product);

        List<ProductStock> stocks = product.getStock();
        if (stocks != null && !stocks.isEmpty()) {
            int totalQuantity = stocks.stream()
                    .mapToInt(ProductStock::getQuantity)
                    .sum();
            ProductStock firstStock = stocks.getFirst();
            ProductStockResponse stockResponse = productStockMapper.toResponse(firstStock);
            stockResponse.setQuantity(totalQuantity);
            response.setStock(stockResponse);
        }

        return response;
    }

    @Transactional
    public ProductResponse updateProduct(int id, ProductUpdateRequest productUpdateRequest) {
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
            BigDecimal oldPrice = product.getPrice();
            BigDecimal newPrice = productUpdateRequest.getPrice();
            if (oldPrice != null && newPrice != null && newPrice.compareTo(oldPrice) < 0) {
                applicationEventPublisher.publishEvent(
                        new ProductPriceDropEvent(product.getId(), product.getName(), oldPrice, newPrice)
                );
            }
            product.setPrice(newPrice);
            changes.add("Price changed from '" + oldPrice + "' to '" + newPrice + "'");
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

        // Sync product images if provided
        if (productUpdateRequest.getImageList() != null) {
            List<ProductImageUpdate> requestedImages = productUpdateRequest.getImageList();
            List<ProductImage> existingImages = productImageRepository.findByProductId(id);

            // Map existing images by URL for quick lookup
            Map<String, ProductImage> existingByUrl = new HashMap<>();
            for (ProductImage existing : existingImages) {
                existingByUrl.put(existing.getImageUrl(), existing);
            }

            Set<String> requestedUrls = new HashSet<>();
            for (ProductImageUpdate dto : requestedImages) {
                requestedUrls.add(dto.getImageUrl());
            }

            // Delete images that are no longer present in the request
            List<ProductImage> toDelete = new ArrayList<>();
            for (ProductImage existing : existingImages) {
                if (!requestedUrls.contains(existing.getImageUrl())) {
                    toDelete.add(existing);
                }
            }
            if (!toDelete.isEmpty()) {
                for (ProductImage image : toDelete) {
                    if (image.getPublicId() != null) {
                        cloudinaryService.delete(image.getPublicId());
                    }
                }
                productImageRepository.deleteAll(toDelete);
            }

            // Upsert requested images (update existing by URL, create new ones when needed)
            List<ProductImage> toSave = new ArrayList<>();
            for (ProductImageUpdate dto : requestedImages) {
                ProductImage image = existingByUrl.get(dto.getImageUrl());
                if (image == null) {
                    image = new ProductImage();
                    image.setProductId(id);
                    image.setImageUrl(dto.getImageUrl());
                    image.setCreatedAt(LocalDateTime.now());
                }
                image.setIsPrimaryImage(dto.getIsPrimaryImage());
                image.setIsActive(true);
                if (dto.getPublicId() != null) {
                    image.setPublicId(dto.getPublicId());
                }
                toSave.add(image);
            }

            if (!toSave.isEmpty()) {
                productImageRepository.saveAll(toSave);
            }

            changes.add("Images updated");
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
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductUpdateRequest productUpdateRequest) {
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

        if (productUpdateRequest.getImageList() != null && !productUpdateRequest.getImageList().isEmpty()) {
            List<ProductImageUpdate> imageUpdates = productUpdateRequest.getImageList();
            List<ProductImage> images = new ArrayList<>();
            for (ProductImageUpdate imageUpdate : imageUpdates) {
                ProductImage image = new ProductImage();
                image.setProductId(savedProduct.getId());
                image.setImageUrl(imageUpdate.getImageUrl());
                image.setIsPrimaryImage(imageUpdate.getIsPrimaryImage());
                image.setPublicId(imageUpdate.getPublicId());
                image.setIsActive(true);
                image.setCreatedAt(LocalDateTime.now());
                images.add(image);
            }
            if (!images.isEmpty()) {
                productImageRepository.saveAll(images);
            }
        }

        ProductResponse response = productMapper.toResponse(savedProduct);

        // Attach total stock across all warehouses if it already exists for this product using JPA relationship
        List<ProductStock> stocks = savedProduct.getStock();
        if (stocks != null && !stocks.isEmpty()) {
            int totalQuantity = stocks.stream()
                    .mapToInt(ProductStock::getQuantity)
                    .sum();
            ProductStock firstStock = stocks.getFirst();
            ProductStockResponse stockResponse = productStockMapper.toResponse(firstStock);
            stockResponse.setQuantity(totalQuantity);
            response.setStock(stockResponse);
        }

        return response;
    }

    @Transactional
    public void deleteProduct(int id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Transactional
    public Product updateProductStatus(int id, boolean isActive) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setIsActive(isActive);
        return productRepository.save(product);
    }

    @Transactional
    public List<ProductImageResponse> addProductImages(int productId, List<MultipartFile> files, Boolean isPrimaryImage) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        List<ProductImage> images = new ArrayList<>();

        if (Boolean.TRUE.equals(isPrimaryImage)) {
            List<ProductImage> existingImages = productImageRepository.findByProductId(productId);
            for (ProductImage existing : existingImages) {
                if (Boolean.TRUE.equals(existing.getIsPrimaryImage())) {
                    existing.setIsPrimaryImage(false);
                }
            }
            productImageRepository.saveAll(existingImages);
        }

        for (MultipartFile file : files) {
            ImageUploadResponse uploadResponse = cloudinaryService.upload(file);
            ProductImage image = new ProductImage();
            image.setProductId(product.getId());
            image.setImageUrl(uploadResponse.getUrl());
            image.setPublicId(uploadResponse.getPublic_id());
            image.setIsPrimaryImage(isPrimaryImage);
            image.setIsActive(true);
            image.setCreatedAt(LocalDateTime.now());
            images.add(image);
        }

        if (!images.isEmpty()) {
            images = productImageRepository.saveAll(images);
        }

        return images.stream()
                .map(productImageMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteProductImage(int productId, int imageId) {
        ProductImage image = productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new RuntimeException("Image not found for product id: " + productId));

        if (image.getPublicId() != null) {
            cloudinaryService.delete(image.getPublicId());
        }

        productImageRepository.delete(image);
    }

    @Transactional(readOnly = true)
    public List<ImageUploadResponse> uploadImages(List<MultipartFile> files) {
        return cloudinaryService.uploadMany(files);
    }

    @Transactional
    public MessageResponse updateProductLiked(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + auth.getName()));

        Optional<ProductLiked> productLiked = productLikedRepository.findByProductIdAndUserId(id, loggedInUser.getId());

        if (productLiked.isPresent()) {
            return new MessageResponse("Product already liked by this user");
        }

        ProductLiked newProductLiked = new ProductLiked(
                loggedInUser,
                productRepository.findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)),
                false
        );

        productLikedRepository.save(newProductLiked);
        return new MessageResponse("Product liked successfully");
    }

    @Transactional
    public MessageResponse deleteProductLiked(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + auth.getName()));

        Optional<ProductLiked> productLiked = productLikedRepository.findByProductIdAndUserId(id, loggedInUser.getId());

        if (productLiked.isEmpty()) {
            return new MessageResponse("Product not liked by this user");
        }

        productLikedRepository.delete(productLiked.get());
        return new MessageResponse("Product unliked successfully");
    }
}
