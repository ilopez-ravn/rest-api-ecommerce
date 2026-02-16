package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.CategoryCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.Entities.Inventory.Category;
import jakarta.validation.constraints.Min;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public ResponseEntity<?> getAllCategories() {
           List<Category> categories = categoryRepository.findByIsActiveTrue();

        return ResponseEntity.ok()
                .body(categories);
    }

    public ResponseEntity<?> createCategory(CategoryCreateRequest categoryCreateRequest) {
        Category category = new Category(
            categoryCreateRequest.getName(),
            categoryCreateRequest.getDescription()
            );

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok()
                .body(savedCategory);
    }

    public ResponseEntity<?> updateCategory(@Min(1) int id, CategoryUpdateRequest categoryUpdateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        category.setName(categoryUpdateRequest.getName());
        category.setDescription(categoryUpdateRequest.getDescription());
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok()
                .body(updatedCategory);
    }

    public ResponseEntity<?> deleteCategory(@Min(1) int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setActive(false);
        Category deletedCategory = categoryRepository.save(category);
        return ResponseEntity.ok()
                .body(deletedCategory);
    }
}
