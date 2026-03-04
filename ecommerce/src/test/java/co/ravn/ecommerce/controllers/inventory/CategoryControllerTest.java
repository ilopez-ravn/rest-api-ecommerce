package co.ravn.ecommerce.controllers.inventory;

import co.ravn.ecommerce.dto.request.inventory.CategoryCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.CategoryResponse;
import co.ravn.ecommerce.exception.GlobalExceptionHandler;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.filters.JwtAuthFilter;
import co.ravn.ecommerce.filters.RateLimitFilter;
import co.ravn.ecommerce.services.inventory.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final String BASE_URL = "/api/v1/categories";

    @Nested
    @DisplayName("GET /api/v1/categories")
    class GetAllCategories {

        @Test
        @DisplayName("returns 200 and list of categories")
        void getAllCategories_returns200() throws Exception {
            // Arrange
            CategoryResponse cat = new CategoryResponse();
            cat.setId(1);
            cat.setName("Electronics");
            cat.setDescription("Tech stuff");
            cat.setIs_active(true);
            when(categoryService.getAllCategories()).thenReturn(List.of(cat));

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Electronics"))
                    .andExpect(jsonPath("$[0].description").value("Tech stuff"));

            verify(categoryService).getAllCategories();
        }

        @Test
        @DisplayName("returns 200 and empty list when no categories")
        void getAllCategories_empty_returns200() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(categoryService).getAllCategories();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/categories")
    class CreateCategory {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and category when valid body")
        void createCategory_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryCreateRequest("Books", "All books"));
            CategoryResponse response = new CategoryResponse();
            response.setId(1);
            response.setName("Books");
            response.setDescription("All books");
            response.setIs_active(true);
            when(categoryService.createCategory(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Books"));

            verify(categoryService).createCategory(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is blank")
        void createCategory_blankName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryCreateRequest("", "x"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message").value("One or more fields have validation errors."));

            verify(categoryService, never()).createCategory(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is null or missing")
        void createCategory_missingName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryCreateRequest(null, "Only description"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(categoryService, never()).createCategory(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name exceeds 100 characters")
        void createCategory_nameTooLong_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryCreateRequest("a".repeat(101), "x"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(categoryService, never()).createCategory(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when body is not valid JSON")
        void createCategory_invalidJson_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("not json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));

            verify(categoryService, never()).createCategory(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categories/{id}")
    class UpdateCategory {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and updated category when valid")
        void updateCategory_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryUpdateRequest("Updated", "Updated desc"));
            CategoryResponse response = new CategoryResponse();
            response.setId(1);
            response.setName("Updated");
            response.setDescription("Updated desc");
            response.setIs_active(true);
            when(categoryService.updateCategory(eq(1), any())).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated"));

            verify(categoryService).updateCategory(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when category not found")
        void updateCategory_notFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryUpdateRequest("X", "Y"));
            when(categoryService.updateCategory(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Category not found"));

            mockMvc.perform(put(BASE_URL + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(categoryService).updateCategory(eq(999), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is blank")
        void updateCategory_blankName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryUpdateRequest("", "desc"));

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(categoryService, never()).updateCategory(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name exceeds 100 characters")
        void updateCategory_nameTooLong_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryUpdateRequest("b".repeat(101), "x"));

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(categoryService, never()).updateCategory(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when path id is invalid (zero)")
        void updateCategory_invalidId_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CategoryUpdateRequest("Valid", "x"));

            mockMvc.perform(put(BASE_URL + "/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(categoryService, never()).updateCategory(eq(0), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{id}")
    class DeleteCategory {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 204 when category exists")
        void deleteCategory_returns204() throws Exception {
            doNothing().when(categoryService).deleteCategory(1);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(categoryService).deleteCategory(1);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when category not found")
        void deleteCategory_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Category not found"))
                    .when(categoryService).deleteCategory(999);

            mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(categoryService).deleteCategory(999);
        }
    }
}
