package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.CategoryCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.CategoryResponse;
import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Entities.Inventory.Product;
import jakarta.validation.constraints.Min;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
        private final ProductRepository productRepository;

    @Autowired
        public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
                this.productRepository = productRepository;
    }

    public ResponseEntity<?> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();

        List<CategoryResponse> response = categories.stream()
                .map(CategoryResponse::new)
                .toList();

        return ResponseEntity.ok()
                .body(response);
    }

    @Transactional
    public ResponseEntity<?> createCategory(CategoryCreateRequest categoryCreateRequest) {
        Category category = new Category(
                categoryCreateRequest.getName(),
                categoryCreateRequest.getDescription()
        );

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok()
                .body(new CategoryResponse(savedCategory));
    }

    @Transactional
    public ResponseEntity<?> updateCategory(@Min(1) int id, CategoryUpdateRequest categoryUpdateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(categoryUpdateRequest.getName());
        category.setDescription(categoryUpdateRequest.getDescription());
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok()
                .body(new CategoryResponse(updatedCategory));
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
