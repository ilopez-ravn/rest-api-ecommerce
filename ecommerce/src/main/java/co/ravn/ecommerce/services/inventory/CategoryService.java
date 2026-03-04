package co.ravn.ecommerce.services.inventory;

import co.ravn.ecommerce.dto.request.inventory.CategoryCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.CategoryResponse;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.inventory.Category;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.inventory.CategoryMapper;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.inventory.CategoryRepository;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException(
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
