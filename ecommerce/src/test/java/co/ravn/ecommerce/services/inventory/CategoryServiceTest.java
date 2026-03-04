package co.ravn.ecommerce.services.inventory;

import co.ravn.ecommerce.dto.request.inventory.CategoryCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.CategoryResponse;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.inventory.Category;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.inventory.CategoryMapper;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.inventory.CategoryRepository;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategories {

        @Test
        @DisplayName("returns list of category responses")
        void returnsList() {
            Category cat = Category.builder().id(1).name("Electronics").description("Tech").isActive(true).build();
            CategoryResponse resp = new CategoryResponse(1, "Electronics", "Tech", Boolean.TRUE);
            when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(cat));
            when(categoryMapper.toResponse(cat)).thenReturn(resp);

            List<CategoryResponse> result = categoryService.getAllCategories();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Electronics");
            verify(categoryRepository).findByIsActiveTrue();
            verify(categoryMapper).toResponse(cat);
        }

        @Test
        @DisplayName("returns empty list when no categories")
        void returnsEmptyList() {
            when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.getAllCategories();

            assertThat(result).isEmpty();
            verify(categoryRepository).findByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("saves and returns category response")
        void createsCategory() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("manager1", null));
            SysUser user = new SysUser();
            user.setId(10);
            user.setUsername("manager1");
            when(userRepository.findByUsernameAndIsActiveTrue("manager1")).thenReturn(Optional.of(user));

            CategoryCreateRequest request = new CategoryCreateRequest("Books", "All books");
            Category entity = Category.builder().id(1).name("Books").description("All books").createdBy(10).build();
            CategoryResponse response = new CategoryResponse(1, "Books", "All books", Boolean.TRUE);

            when(categoryMapper.toEntity(request)).thenReturn(entity);
            when(categoryRepository.save(any(Category.class))).thenReturn(entity);
            when(categoryMapper.toResponse(entity)).thenReturn(response);

            CategoryResponse result = categoryService.createCategory(request);

            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Books");
            verify(categoryRepository).save(entity);
            verify(categoryMapper).toResponse(entity);
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("updates and returns category when found")
        void updatesWhenFound() {
            Category category = Category.builder().id(1).name("Old").description("Old desc").build();
            CategoryUpdateRequest request = new CategoryUpdateRequest("New", "New desc");
            CategoryResponse response = new CategoryResponse(1, "New", "New desc", Boolean.TRUE);

            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            doNothing().when(categoryMapper).updateFromRequest(request, category);
            when(categoryRepository.save(category)).thenReturn(category);
            when(categoryMapper.toResponse(category)).thenReturn(response);

            CategoryResponse result = categoryService.updateCategory(1, request);

            assertThat(result.getName()).isEqualTo("New");
            verify(categoryRepository).findById(1);
            verify(categoryMapper).updateFromRequest(request, category);
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category not found")
        void throwsWhenNotFound() {
            when(categoryRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(99, new CategoryUpdateRequest("X", "Y")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");
            verify(categoryRepository).findById(99);
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("deactivates category and removes from products when found")
        void deactivatesWhenFound() {
            Category category = Category.builder().id(1).name("Cat").description("Desc").isActive(true).build();
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            when(productRepository.findAllByCategories_Id(1)).thenReturn(List.of());
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryService.deleteCategory(1);

            verify(categoryRepository).findById(1);
            assertThat(category.isActive()).isFalse();
            verify(productRepository).findAllByCategories_Id(1);
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category not found")
        void throwsWhenNotFound() {
            when(categoryRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");
            verify(categoryRepository).findById(99);
            verify(categoryRepository, never()).save(any());
        }
    }
}
