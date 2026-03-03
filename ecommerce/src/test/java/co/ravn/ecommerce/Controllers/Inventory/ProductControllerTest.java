package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.ProductImageUpdate;
import co.ravn.ecommerce.DTO.Request.Inventory.ProductUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.CategoryResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductCursorPage;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductImageResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.ProductResponse;
import co.ravn.ecommerce.DTO.Response.Inventory.TagResponse;
import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Entities.Inventory.ProductImage;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Exception.GlobalExceptionHandler;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Filters.JwtAuthFilter;
import co.ravn.ecommerce.Filters.RateLimitFilter;
import co.ravn.ecommerce.Services.Inventory.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final String BASE_URL = "/api/v1/products";

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetFilteredProducts {

        @Test
        @DisplayName("returns 200 and cursor page when no params")
        void getProducts_cursorPaginated_returns200() throws Exception {
            ProductResponse product = new ProductResponse();
            product.setId(1);
            product.setName("Widget");
            product.setPrice(BigDecimal.TEN);
            ProductCursorPage page = new ProductCursorPage(List.of(product), 2, true, 100L);
            when(productService.getFilteredProductsCursor(any(), eq(null), eq(20))).thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Widget"))
                    .andExpect(jsonPath("$.hasMore").value(true))
                    .andExpect(jsonPath("$.totalItems").value(100));

            verify(productService).getFilteredProductsCursor(any(), eq(null), eq(20));
        }

        @Test
        @DisplayName("returns 200 when filters applied")
        void getProducts_withFilters_returns200() throws Exception {
            ProductCursorPage page = new ProductCursorPage(List.of(), null, false, 0L);
            when(productService.getFilteredProductsCursor(any(), eq(null), eq(10)))
                    .thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .param("categories_id", "1", "2")
                            .param("min_price", "5")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.hasMore").value(false));

            verify(productService).getFilteredProductsCursor(any(), eq(null), eq(10));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductById {

        @Test
        @DisplayName("returns 200 and product when exists")
        void getProductById_exists_returns200() throws Exception {
            ProductResponse response = new ProductResponse();
            response.setId(1);
            response.setName("Widget");
            response.setPrice(BigDecimal.TEN);
            when(productService.getProductById(1)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Widget"));

            verify(productService).getProductById(1);
        }

        @Test
        @DisplayName("returns 404 when product not found")
        void getProductById_notFound_returns404() throws Exception {
            when(productService.getProductById(999))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(productService).getProductById(999);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProduct {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and product when valid body")
        void createProduct_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(
                    new ProductUpdateRequest("New Product", "Desc", new BigDecimal("19.99"), null, null, null, null));
            ProductResponse response = ProductResponse.builder().id(1).name("New Product").price(new BigDecimal("19.99")).build();
            when(productService.createProduct(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("New Product"));

            verify(productService).createProduct(any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/products/{id}")
    class UpdateProduct {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and updated product when valid")
        void updateProduct_valid_returns200() throws Exception {
            ProductResponse response = ProductResponse.builder()
                    .id(1)
                    .name("Updated")
                    .description("Updated desc")
                    .price(new BigDecimal("29.99"))
                    .categories(List.of(CategoryResponse.builder()
                            .id(1).name("Category 1").description("Category 1 description").build()))
                    .tags(List.of(TagResponse.builder()
                            .id(1).name("Tag 1").is_active(true).build()))
                    .product_images(List.of(ProductImageResponse.builder()
                            .id(1).image_url("https://example.com/image.jpg").build()))
                    .build();

            Category category = Category.builder().id(1).name("Category 1").description("Category 1 description").build();
            Tag tag = Tag.builder().id(1).name("Tag 1").isActive(true).build();
            ProductImage productImage = ProductImage.builder()
                    .imageUrl("https://example.com/image.jpg")
                    .isPrimaryImage(true)
                    .publicId("img123")
                    .build();

            ProductUpdateRequest request = ProductUpdateRequest.builder()
                    .name("Updated")
                    .description("Updated desc")
                    .price(new BigDecimal("29.99"))
                    .categoryList(List.of(category.getId()))
                    .tagList(List.of(tag.getId()))
                    .imageList(List.of(new ProductImageUpdate(
                            productImage.getImageUrl(),
                            productImage.getIsPrimaryImage(),
                            productImage.getPublicId())))
                    .isActive(Boolean.TRUE)
                    .build();

            String body = objectMapper.writeValueAsString(request);

            when(productService.updateProduct(eq(1), any(ProductUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(patch(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated"))
                    .andExpect(jsonPath("$.description").value("Updated desc"))
                    .andExpect(jsonPath("$.price").value(29.99))
                    .andExpect(jsonPath("$.categories[0].id").value(1))
                    .andExpect(jsonPath("$.categories[0].name").value("Category 1"))
                    .andExpect(jsonPath("$.tags[0].id").value(1))
                    .andExpect(jsonPath("$.tags[0].name").value("Tag 1"))
                    .andExpect(jsonPath("$.product_images[0].id").value(1))
                    .andExpect(jsonPath("$.product_images[0].image_url").value("https://example.com/image.jpg"));

            verify(productService).updateProduct(eq(1), argThat(req ->
                    "Updated".equals(req.getName())
                            && "Updated desc".equals(req.getDescription())
                            && new BigDecimal("29.99").compareTo(req.getPrice()) == 0
                            && req.getCategoryList().equals(List.of(category.getId()))
                            && req.getTagList().equals(List.of(tag.getId()))
                            && req.getImageList() != null
                            && req.getImageList().size() == 1
                            && "https://example.com/image.jpg".equals(req.getImageList().getFirst().getImageUrl())
                            && Boolean.TRUE.equals(req.getIsActive())
            ));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when product not found")
        void updateProduct_notFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(
                    new ProductUpdateRequest("X", null, new BigDecimal("1"), null, null, null, null));
            when(productService.updateProduct(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            mockMvc.perform(patch(BASE_URL + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(productService).updateProduct(eq(999), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProduct {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 204 when product exists")
        void deleteProduct_returns204() throws Exception {
            doNothing().when(productService).deleteProduct(1);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(productService).deleteProduct(1);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when product not found")
        void deleteProduct_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Product not found"))
                    .when(productService).deleteProduct(999);

            mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(productService).deleteProduct(999);
        }
    }
}
