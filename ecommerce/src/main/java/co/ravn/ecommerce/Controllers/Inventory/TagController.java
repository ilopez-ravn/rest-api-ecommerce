package co.ravn.ecommerce.Controllers.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.TagCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.TagUpdateRequest;
import co.ravn.ecommerce.Services.Inventory.TagService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/tags")
public class TagController {

    private TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("")
    private ResponseEntity<?> getAllTags() {
        return tagService.getAllTags();
    }

    @PostMapping("")
    private ResponseEntity<?> createTag(@RequestBody TagCreateRequest tagCreateRequest) {
        return tagService.createTag(tagCreateRequest);
    }

    @PutMapping("/{id}")
    private ResponseEntity<?> updateTag(@PathVariable @Min(1) int id, @RequestBody TagUpdateRequest tagUpdateRequest) {
        return tagService.updateTag(id, tagUpdateRequest);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<?> deleteTag(@PathVariable @Min(1) int id) {
        return tagService.deleteTag(id);
    }

}
