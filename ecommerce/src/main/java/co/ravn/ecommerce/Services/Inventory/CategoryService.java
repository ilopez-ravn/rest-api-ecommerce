package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.CategoryCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.CategoryResponse;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Mappers.Inventory.CategoryMapper;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;

@Service
@AllArgsConstructor
@Slf4j
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;

    public ResponseEntity<?> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();

        List<CategoryResponse> response = categories.stream()
                .map(categoryMapper::toResponse)
                .toList();

        return ResponseEntity.ok()
                .body(response);
    }

    @Transactional
    public ResponseEntity<?> createCategory(@Valid CategoryCreateRequest categoryCreateRequest) {
        Category category = categoryMapper.toEntity(categoryCreateRequest);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + auth.getName()));

        category.setCreatedBy(loggedInUser.getId());

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok()
                .body(categoryMapper.toResponse(savedCategory));
    }

    @Transactional
    public ResponseEntity<?> updateCategory(@Min(1) int id, @Valid CategoryUpdateRequest categoryUpdateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        categoryMapper.updateFromRequest(categoryUpdateRequest, category);
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok()
                .body(categoryMapper.toResponse(updatedCategory));
    }

    @Transactional
    public ResponseEntity<?> deleteCategory(@Min(1) int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setActive(false);

        // remove category from products that have it assigned
        List<Product> products = productRepository.findAllByCategories_Id(id);
        for (Product product : products) {
            if (product.getCategories() != null) {
                product.getCategories().removeIf(c -> c.getId() == id);
            }
        }
        productRepository.saveAll(products);

        categoryRepository.save(category);
        return ResponseEntity.noContent().build();
    }
}
