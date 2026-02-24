package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.CategoryCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.CategoryResponse;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Inventory.CategoryMapper;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categories.stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest categoryCreateRequest) {
        Category category = categoryMapper.toEntity(categoryCreateRequest);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser loggedInUser = userRepository.findByUsernameAndIsActiveTrue(auth.getName())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + auth.getName()));

        category.setCreatedBy(loggedInUser.getId());

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(int id, CategoryUpdateRequest categoryUpdateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        categoryMapper.updateFromRequest(categoryUpdateRequest, category);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(int id) {
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
    }
}
