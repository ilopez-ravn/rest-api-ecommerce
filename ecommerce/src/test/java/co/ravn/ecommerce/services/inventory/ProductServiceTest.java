package co.ravn.ecommerce.services.inventory;

import co.ravn.ecommerce.dto.ProductPriceDropEvent;
import co.ravn.ecommerce.dto.request.inventory.ProductFilterRequest;
import co.ravn.ecommerce.dto.request.inventory.ProductImageUpdate;
import co.ravn.ecommerce.dto.request.inventory.ProductUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.CategoryResponse;
import co.ravn.ecommerce.dto.response.inventory.ImageUploadResponse;
import co.ravn.ecommerce.dto.response.inventory.ProductImageResponse;
import co.ravn.ecommerce.dto.response.inventory.ProductCursorPage;
import co.ravn.ecommerce.dto.response.inventory.ProductResponse;
import co.ravn.ecommerce.dto.response.inventory.ProductStockResponse;
import co.ravn.ecommerce.dto.response.inventory.TagResponse;
import co.ravn.ecommerce.dto.response.MessageResponse;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.cart.ProductLiked;
import co.ravn.ecommerce.entities.inventory.Category;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.entities.inventory.ProductChangesLog;
import co.ravn.ecommerce.entities.inventory.ProductImage;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import co.ravn.ecommerce.entities.inventory.Tag;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.inventory.ProductImageMapper;
import co.ravn.ecommerce.mappers.inventory.ProductMapper;
import co.ravn.ecommerce.mappers.inventory.ProductStockMapper;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.cart.ProductLikedRepository;
import co.ravn.ecommerce.repositories.inventory.CategoryRepository;
import co.ravn.ecommerce.repositories.inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.repositories.inventory.ProductImageRepository;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import co.ravn.ecommerce.repositories.inventory.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductChangesLogRepository productChangesLogRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProductLikedRepository productLikedRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private ProductStockMapper productStockMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ProductService productService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("returns product response when found")
        void returnsResponseWhenFound() {
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            product.setPrice(BigDecimal.TEN);
            product.setStock(List.of());
            ProductResponse response = new ProductResponse();
            response.setId(1);
            response.setName("Widget");
            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(response);

            ProductResponse result = productService.getProductById(1);

            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Widget");
            verify(productRepository).findByIdAndDeletedAtIsNull(1);
        }

        @Test
        @DisplayName("throws when product not found")
        void throwsWhenNotFound() {
            when(productRepository.findByIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
            verify(productRepository).findByIdAndDeletedAtIsNull(99);
        }

        @Test
        @DisplayName("attaches total stock when product has stock")
        void attachesStockWhenPresent() {
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            ProductStock ps = new ProductStock();
            ps.setQuantity(10);
            product.setStock(List.of(ps));
            ProductResponse response = new ProductResponse();
            response.setId(1);
            ProductStockResponse stockResp = new ProductStockResponse();
            stockResp.setQuantity(10);
            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(response);
            when(productStockMapper.toResponse(ps)).thenReturn(stockResp);

            ProductResponse result = productService.getProductById(1);

            assertThat(result.getStock()).isNotNull();
            assertThat(result.getStock().getQuantity()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("saves product and returns response when minimal fields provided")
        void savesAndReturnsResponse() {
            ProductUpdateRequest request = new ProductUpdateRequest("Widget", "A widget", BigDecimal.TEN, null, null, null, true);
            Product saved = new Product();
            saved.setId(1);
            saved.setName("Widget");
            saved.setStock(List.of());
            ProductResponse response = new ProductResponse();
            response.setId(1);
            response.setName("Widget");

            when(productRepository.save(any(Product.class))).thenReturn(saved);
            when(productMapper.toResponse(saved)).thenReturn(response);

            ProductResponse result = productService.createProduct(request);

            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Widget");
            verify(productRepository).save(any(Product.class));
            verify(productMapper).toResponse(saved);
        }

        @Test
        @DisplayName("fetches tags and categories from repositories when ids are provided")
        void fetchesTagsAndCategories() {
            Tag tag = Tag.builder().id(1).name("Tag 1").isActive(true).build();
            Category cat = Category.builder().id(1).name("Books").description("All books").createdBy(1).build();
            ProductUpdateRequest request = new ProductUpdateRequest("Widget", null, BigDecimal.TEN, List.of(1), List.of(1), null, true);
            Product saved = Product.builder().id(1).name("Widget").tags(List.of(tag)).categories(List.of(cat)).stock(List.of()).build();

            ProductResponse response = new ProductResponse();
            response.setId(1);

            when(tagRepository.findAllByIdInAndIsActiveTrue(List.of(1))).thenReturn(List.of(tag));
            when(categoryRepository.findAllByIdInAndIsActiveTrue(List.of(1))).thenReturn(List.of(cat));
            when(productRepository.save(any(Product.class))).thenReturn(saved);
            when(productMapper.toResponse(saved)).thenReturn(response);

            ProductResponse result = productService.createProduct(request);

            assertThat(result.getId()).isEqualTo(1);
            verify(tagRepository).findAllByIdInAndIsActiveTrue(List.of(1));
            verify(categoryRepository).findAllByIdInAndIsActiveTrue(List.of(1));
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("sets deletedAt and saves when product found")
        void setsDeletedAtWhenFound() {
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            productService.deleteProduct(1);

            assertThat(product.getDeletedAt()).isNotNull();
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void throwsWhenNotFound() {
            when(productRepository.findByIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void throwsWhenNotFound() {
            when(productRepository.findByIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(99,
                    new ProductUpdateRequest(null, null, null, null, null, null, null)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("saves product without writing a change log when no fields are modified")
        void savesWithoutLogWhenNoFieldsChange() {
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            product.setPrice(BigDecimal.TEN);
            product.setIsActive(true);
            ProductUpdateRequest request = new ProductUpdateRequest(null, null, null, null, null, null, null);
            ProductResponse response = new ProductResponse();

            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(response);

            productService.updateProduct(1, request);

            verify(productRepository).save(product);
            verify(productChangesLogRepository, never()).save(any());
            verify(userRepository, never()).findByUsernameAndIsActiveTrue(any());
        }

        @Test
        @DisplayName("publishes price drop event when new price is lower than current price")
        void publishesPriceDropEventOnPriceDecrease() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("manager1", null));
            SysUser user = new SysUser();
            user.setId(10);
            user.setUsername("manager1");

            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            product.setPrice(new BigDecimal("20.00"));
            ProductUpdateRequest request = new ProductUpdateRequest(null, null, new BigDecimal("10.00"), null, null, null, null);
            ProductResponse response = new ProductResponse();

            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(response);

            productService.updateProduct(1, request);

            verify(applicationEventPublisher).publishEvent(any(ProductPriceDropEvent.class));
            verify(productChangesLogRepository).save(any(ProductChangesLog.class));
        }

        @Test
        @DisplayName("updates name, writes a change log, and saves product")
        void updatesNameAndLogsChange() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("manager1", null));
            SysUser user = new SysUser();
            user.setId(10);
            user.setUsername("manager1");

            Product product = new Product();
            product.setId(1);
            product.setName("OldName");
            ProductUpdateRequest request = new ProductUpdateRequest("NewName", null, null, null, null, null, null);
            ProductResponse response = new ProductResponse();

            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(response);

            productService.updateProduct(1, request);

            assertThat(product.getName()).isEqualTo("NewName");
            verify(productChangesLogRepository).save(any(ProductChangesLog.class));
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("updates product details including categories, tags, images and isActive")
        void updatesDetailsCategoriesTagsImagesAndStatus() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("manager1", null));
            SysUser user = new SysUser();
            user.setId(10);
            user.setUsername("manager1");

            Product product = Product.builder()
                    .id(1)
                    .name("OldName")
                    .description("Old description")
                    .price(new BigDecimal("10.00"))
                    .isActive(false)
                    .tags(new ArrayList<>())
                    .categories(new ArrayList<>())
                    .build();

            Tag tag = Tag.builder().id(1).name("Tag 1").isActive(true).build();
            Category category = Category.builder().id(1).name("Category 1").description("Category 1 description").build();

            ProductImage existingImage = ProductImage.builder().id(5).productId(1).imageUrl("https://old.com/old.jpg").isPrimaryImage(true).publicId("old_pub").build();

            ProductImageUpdate imageUpdate = ProductImageUpdate.builder()
                    .imageUrl("https://example.com/image.jpg")
                    .isPrimaryImage(true)
                    .publicId("img123")
                    .build();

            ProductUpdateRequest request = ProductUpdateRequest.builder()
                    .name("NewName")
                    .description("New description")
                    .price(new BigDecimal("20.00"))
                    .categoryList(List.of(category.getId()))
                    .tagList(List.of(tag.getId()))
                    .imageList(List.of(imageUpdate))
                    .isActive(Boolean.TRUE)
                    .build();

            ProductResponse mappedResponse = ProductResponse.builder().id(1)
            .name("NewName")
            .description("New description")
            .price(new BigDecimal("20.00"))
            .categories(List.of(CategoryResponse.builder().id(1).name("Category 1").description("Category 1 description").build()))
            .tags(List.of(TagResponse.builder().id(1).name("Tag 1").is_active(true).build()))
            .product_images(List.of(ProductImageResponse.builder().id(1).image_url("https://example.com/image.jpg").is_primary_image(true).public_id("img123").build()))
            .build();

            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));
            when(tagRepository.findAllByIdInAndIsActiveTrue(List.of(tag.getId()))).thenReturn(List.of(tag));
            when(categoryRepository.findAllByIdInAndIsActiveTrue(List.of(category.getId()))).thenReturn(List.of(category));
            when(productImageRepository.findByProductId(1)).thenReturn(List.of(existingImage));
            when(productImageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(productRepository.save(product)).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(mappedResponse);

            ProductResponse result = productService.updateProduct(1, request);

            assertThat(result).isSameAs(mappedResponse);
            assertThat(product.getName()).isEqualTo("NewName");
            assertThat(product.getDescription()).isEqualTo("New description");
            assertThat(product.getPrice()).isEqualByComparingTo("20.00");
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getTags()).containsExactly(tag);
            assertThat(product.getCategories()).containsExactly(category);

            verify(tagRepository).findAllByIdInAndIsActiveTrue(List.of(tag.getId()));
            verify(categoryRepository).findAllByIdInAndIsActiveTrue(List.of(category.getId()));
            verify(productImageRepository).findByProductId(1);
            verify(productImageRepository).deleteAll(List.of(existingImage));
            verify(productImageRepository).saveAll(anyList());
            verify(productChangesLogRepository).save(any(ProductChangesLog.class));
            verify(productRepository).save(product);
        }
    }

    @Nested
    @DisplayName("updateProductStatus")
    class UpdateProductStatus {

        @Test
        @DisplayName("sets isActive flag and saves product when found")
        void setsIsActiveAndSaves() {
            Product product = new Product();
            product.setId(1);
            product.setIsActive(false);
            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(productRepository.save(product)).thenReturn(product);

            productService.updateProductStatus(1, true);

            assertThat(product.getIsActive()).isTrue();
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void throwsWhenNotFound() {
            when(productRepository.findByIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProductStatus(99, true))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("addProductImages")
    class AddProductImages {

        @Test
        @DisplayName("uploads files, saves images, and returns responses")
        void uploadsAndSavesImages() {
            Product product = Product.builder().id(5).name("Product 5").description("Product 5 description").price(BigDecimal.TEN).isActive(true).createdAt(LocalDateTime.now()).build();
            MultipartFile file = mock(MultipartFile.class);
            ImageUploadResponse uploadResp = new ImageUploadResponse("https://img.com/x.jpg", "pub123");
            ProductImage savedImage = ProductImage.builder().id(1).imageUrl("https://img.com/x.jpg").isPrimaryImage(false).productId(5).build();
            ProductImageResponse imageResponse = ProductImageResponse.builder().id(1).image_url("https://img.com/x.jpg").product_id(5).public_id("pub123").is_primary_image(false).is_active(true).created_at(LocalDateTime.now()).build();

            when(productRepository.findByIdAndDeletedAtIsNull(5)).thenReturn(Optional.of(product));
            when(cloudinaryService.upload(file)).thenReturn(uploadResp);
            when(productImageRepository.saveAll(anyList())).thenReturn(List.of(savedImage));
            when(productImageMapper.toResponse(savedImage)).thenReturn(imageResponse);

            List<ProductImageResponse> result = productService.addProductImages(5, List.of(file));

            assertThat(result).hasSize(1);
            verify(cloudinaryService).upload(file);
            verify(productImageRepository, times(2)).saveAll(anyList());
        }


        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void throwsWhenProductNotFound() {
            when(productRepository.findByIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.addProductImages(99, List.of()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    @Nested
    @DisplayName("deleteProductImage")
    class DeleteProductImage {

        @Test
        @DisplayName("deletes image and calls Cloudinary cleanup when publicId is set")
        void deletesImageWithCloudinaryCleanup() {
            ProductImage image = new ProductImage();
            image.setId(1);
            image.setPublicId("pub123");
            when(productImageRepository.findByIdAndProductId(1, 5)).thenReturn(Optional.of(image));
            doNothing().when(cloudinaryService).delete("pub123");

            productService.deleteProductImage(5, 1);

            verify(cloudinaryService).delete("pub123");
            verify(productImageRepository).delete(image);
        }

        @Test
        @DisplayName("deletes image without Cloudinary call when publicId is null")
        void deletesImageWithoutCloudinaryWhenNoPublicId() {
            ProductImage image = new ProductImage();
            image.setId(1);
            image.setPublicId(null);
            when(productImageRepository.findByIdAndProductId(1, 5)).thenReturn(Optional.of(image));

            productService.deleteProductImage(5, 1);

            verify(cloudinaryService, never()).delete(any());
            verify(productImageRepository).delete(image);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when image not found for product")
        void throwsWhenImageNotFound() {
            when(productImageRepository.findByIdAndProductId(99, 5)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProductImage(5, 99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Image not found");
            verify(productImageRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("uploadImages")
    class UploadImages {

        @Test
        @DisplayName("delegates to cloudinaryService and returns results")
        void delegatesToCloudinary() {
            MultipartFile file = mock(MultipartFile.class);
            ImageUploadResponse resp = new ImageUploadResponse("https://img.com/x.jpg", "pub1");
            when(cloudinaryService.uploadMany(List.of(file))).thenReturn(List.of(resp));

            List<ImageUploadResponse> result = productService.uploadImages(List.of(file));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPublic_id()).isEqualTo("pub1");
            verify(cloudinaryService).uploadMany(List.of(file));
        }
    }

    @Nested
    @DisplayName("getFilteredProductsCursor")
    class GetFilteredProductsCursor {

        @Test
        @DisplayName("returns cursor page with products and hasMore=false when page fits")
        void returnsCursorPageWhenFits() {
            ProductFilterRequest filter = new ProductFilterRequest();
            Product product = new Product();
            product.setId(1);
            product.setStock(List.of());
            Page<Product> page = new PageImpl<>(List.of(product));
            ProductResponse resp = new ProductResponse();

            when(productRepository.count(any(Specification.class))).thenReturn(1L);
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(productMapper.toResponse(product)).thenReturn(resp);

            ProductCursorPage result = productService.getFilteredProductsCursor(filter, null, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.isHasMore()).isFalse();
            assertThat(result.getTotalItems()).isEqualTo(1L);
            assertThat(result.getNextCursor()).isNull();
        }

        @Test
        @DisplayName("sets hasMore=true and nextCursor when more products exist beyond page size")
        void setsHasMoreAndNextCursor() {
            ProductFilterRequest filter = new ProductFilterRequest();
            Product p1 = new Product(); p1.setId(1); p1.setStock(List.of());
            Product p2 = new Product(); p2.setId(2); p2.setStock(List.of());
            // Returns 2 products for a page size of 1 (pageSize+1 trick indicates hasMore)
            Page<Product> page = new PageImpl<>(List.of(p1, p2));
            ProductResponse resp1 = new ProductResponse(); resp1.setId(1);

            when(productRepository.count(any(Specification.class))).thenReturn(5L);
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(productMapper.toResponse(p1)).thenReturn(resp1);

            ProductCursorPage result = productService.getFilteredProductsCursor(filter, null, 1);

            assertThat(result.isHasMore()).isTrue();
            assertThat(result.getNextCursor()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateProductLiked")
    class UpdateProductLiked {

        @Test
        @DisplayName("returns 'already liked' message when product is already liked by user")
        void returnsAlreadyLikedMessage() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user1", null));
            SysUser user = new SysUser();
            user.setId(10);
            ProductLiked existing = new ProductLiked();

            when(userRepository.findByUsernameAndIsActiveTrue("user1")).thenReturn(Optional.of(user));
            when(productLikedRepository.findByProductIdAndUserId(1, 10)).thenReturn(Optional.of(existing));

            MessageResponse result = productService.updateProductLiked(1);

            assertThat(result.getMessage()).contains("already liked");
            verify(productLikedRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates and saves new like when product not yet liked")
        void createsNewLike() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user1", null));
            SysUser user = new SysUser();
            user.setId(10);
            Product product = new Product();
            product.setId(1);

            when(userRepository.findByUsernameAndIsActiveTrue("user1")).thenReturn(Optional.of(user));
            when(productLikedRepository.findByProductIdAndUserId(1, 10)).thenReturn(Optional.empty());
            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(product));
            when(productLikedRepository.save(any(ProductLiked.class))).thenAnswer(inv -> inv.getArgument(0));

            MessageResponse result = productService.updateProductLiked(1);

            assertThat(result.getMessage()).contains("liked successfully");
            verify(productLikedRepository).save(any(ProductLiked.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void throwsWhenProductNotFound() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user1", null));
            SysUser user = new SysUser();
            user.setId(10);

            when(userRepository.findByUsernameAndIsActiveTrue("user1")).thenReturn(Optional.of(user));
            when(productLikedRepository.findByProductIdAndUserId(1, 10)).thenReturn(Optional.empty());
            when(productRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProductLiked(1))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    @Nested
    @DisplayName("deleteProductLiked")
    class DeleteProductLiked {

        @Test
        @DisplayName("returns 'not liked' message when product was not liked by user")
        void returnsNotLikedMessage() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user1", null));
            SysUser user = new SysUser();
            user.setId(10);

            when(userRepository.findByUsernameAndIsActiveTrue("user1")).thenReturn(Optional.of(user));
            when(productLikedRepository.findByProductIdAndUserId(1, 10)).thenReturn(Optional.empty());

            MessageResponse result = productService.deleteProductLiked(1);

            assertThat(result.getMessage()).contains("not liked");
            verify(productLikedRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes the like and returns success message")
        void deletesLike() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user1", null));
            SysUser user = new SysUser();
            user.setId(10);
            ProductLiked existing = new ProductLiked();

            when(userRepository.findByUsernameAndIsActiveTrue("user1")).thenReturn(Optional.of(user));
            when(productLikedRepository.findByProductIdAndUserId(1, 10)).thenReturn(Optional.of(existing));
            doNothing().when(productLikedRepository).delete(existing);

            MessageResponse result = productService.deleteProductLiked(1);

            assertThat(result.getMessage()).contains("unliked");
            verify(productLikedRepository).delete(existing);
        }
    }
}
