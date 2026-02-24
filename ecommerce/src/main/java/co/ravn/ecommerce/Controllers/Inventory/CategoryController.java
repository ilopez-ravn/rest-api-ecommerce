package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.CategoryCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.CategoryUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.CategoryResponse;
import co.ravn.ecommerce.Services.Inventory.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@AllArgsConstructor
@RequestMapping("api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping("")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryCreateRequest categoryCreateRequest) {
        return ResponseEntity.ok(categoryService.createCategory(categoryCreateRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable @Min(1) int id, @RequestBody @Valid CategoryUpdateRequest categoryUpdateRequest) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable @Min(1) int id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
