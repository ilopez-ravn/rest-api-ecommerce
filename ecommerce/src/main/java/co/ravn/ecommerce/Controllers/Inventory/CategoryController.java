package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.CategoryCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.Services.Inventory.CategoryService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("")
    private ResponseEntity<?> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping("")
    private ResponseEntity<?> createCategory(@RequestBody CategoryCreateRequest categoryCreateRequest) {
        return categoryService.createCategory(categoryCreateRequest);
    }

    @PutMapping("/{id}")
    private ResponseEntity<?> updateCategory(@PathVariable @Min(1) int id, @RequestBody CategoryUpdateRequest categoryUpdateRequest) {
        return categoryService.updateCategory(id, categoryUpdateRequest);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<?> deleteCategory(@PathVariable @Min(1) int id) {
        return categoryService.deleteCategory(id);
    }
    
}
