package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.TagCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.TagUpdateRequest;
import co.ravn.ecommerce.Services.Inventory.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping("")
    public ResponseEntity<?> getAllTags() {
        return tagService.getAllTags();
    }

    @PostMapping("")
    public ResponseEntity<?> createTag(@RequestBody @Valid TagCreateRequest tagCreateRequest) {
        return tagService.createTag(tagCreateRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(@PathVariable @Min(1) int id, @RequestBody @Valid TagUpdateRequest tagUpdateRequest) {
        return tagService.updateTag(id, tagUpdateRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable @Min(1) int id) {
        return tagService.deleteTag(id);
    }

}
