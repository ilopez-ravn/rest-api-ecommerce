package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.ProductPriceDropEvent;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductStockResponse;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductChangesLog;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Inventory.ProductImageMapper;
import co.ravn.ecommerce.Mappers.Inventory.ProductMapper;
import co.ravn.ecommerce.Mappers.Inventory.ProductStockMapper;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Cart.ProductLikedRepository;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductChangesLogRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductImageRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.TagRepository;
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
import java.util.List;
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
            Tag tag = new Tag();
            tag.setId(1);
            Category cat = new Category("Books", "All books");
            cat.setId(1);
            ProductUpdateRequest request = new ProductUpdateRequest("Widget", null, BigDecimal.TEN, List.of(1), List.of(1), null, true);
            Product saved = new Product();
            saved.setId(1);
            saved.setName("Widget");
            saved.setTags(List.of(tag));
            saved.setCategories(List.of(cat));
            saved.setStock(List.of());
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
    }
}
