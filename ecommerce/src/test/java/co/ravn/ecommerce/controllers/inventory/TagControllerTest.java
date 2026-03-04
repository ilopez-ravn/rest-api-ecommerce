package co.ravn.ecommerce.controllers.inventory;

import co.ravn.ecommerce.dto.request.inventory.TagCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.TagUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.TagResponse;
import co.ravn.ecommerce.exception.GlobalExceptionHandler;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.filters.JwtAuthFilter;
import co.ravn.ecommerce.filters.RateLimitFilter;
import co.ravn.ecommerce.services.inventory.TagService;
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

@WebMvcTest(TagController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagService tagService;

    private static final String BASE_URL = "/api/v1/tags";

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @Nested
    @DisplayName("GET /api/v1/tags")
    class GetAllTags {

        @Test
        @DisplayName("returns 200 and list of tags")
        void getAllTags_returns200() throws Exception {
            // Arrange
            TagResponse tag = new TagResponse(1, "sale", true);
            when(tagService.getAllTags()).thenReturn(List.of(tag));

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("sale"))
                    .andExpect(jsonPath("$[0].is_active").value(true));

            verify(tagService).getAllTags();
        }

        @Test
        @DisplayName("returns 200 and empty list when no tags")
        void getAllTags_empty_returns200() throws Exception {
            when(tagService.getAllTags()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(tagService).getAllTags();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/tags")
    class CreateTag {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and tag when valid body")
        void createTag_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new TagCreateRequest("featured"));
            TagResponse response = new TagResponse(1, "featured", true);
            when(tagService.createTag(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("featured"));

            verify(tagService).createTag(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is blank")
        void createTag_blankName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new TagCreateRequest(""));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message").value("One or more fields have validation errors."));

            verify(tagService, never()).createTag(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is null or missing")
        void createTag_missingName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new TagCreateRequest(null));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(tagService, never()).createTag(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name exceeds 100 characters")
        void createTag_nameTooLong_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new TagCreateRequest("a".repeat(101)));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(tagService, never()).createTag(any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when body is not valid JSON")
        void createTag_invalidJson_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("not json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));

            verify(tagService, never()).createTag(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/tags/{id}")
    class UpdateTag {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and updated tag when valid")
        void updateTag_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new TagUpdateRequest("updated-tag"));
            TagResponse response = new TagResponse(1, "updated-tag", true);
            when(tagService.updateTag(eq(1), any())).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("updated-tag"));

            verify(tagService).updateTag(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when tag not found")
        void updateTag_notFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(new TagUpdateRequest("x"));
            when(tagService.updateTag(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Tag not found"));

            mockMvc.perform(put(BASE_URL + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(tagService).updateTag(eq(999), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name is blank")
        void updateTag_blankName_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new TagUpdateRequest(""));

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(tagService, never()).updateTag(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when name exceeds 100 characters")
        void updateTag_nameTooLong_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new TagUpdateRequest("x".repeat(101)));

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(tagService, never()).updateTag(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when path id is invalid (zero)")
        void updateTag_invalidId_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new TagUpdateRequest("valid"));

            mockMvc.perform(put(BASE_URL + "/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(tagService, never()).updateTag(eq(0), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tags/{id}")
    class DeleteTag {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 204 when tag exists")
        void deleteTag_returns204() throws Exception {
            doNothing().when(tagService).deleteTag(1);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(tagService).deleteTag(1);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when tag not found")
        void deleteTag_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Tag not found"))
                    .when(tagService).deleteTag(999);

            mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(tagService).deleteTag(999);
        }
    }
}
