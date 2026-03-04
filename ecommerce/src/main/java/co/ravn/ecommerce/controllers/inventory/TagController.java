package co.ravn.ecommerce.controllers.inventory;

import co.ravn.ecommerce.dto.request.inventory.TagCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.TagUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.TagResponse;
import co.ravn.ecommerce.services.inventory.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping("")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping("")
    public ResponseEntity<TagResponse> createTag(@RequestBody @Valid TagCreateRequest tagCreateRequest) {
        return ResponseEntity.ok(tagService.createTag(tagCreateRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(@PathVariable @Min(1) int id, @RequestBody @Valid TagUpdateRequest tagUpdateRequest) {
        return ResponseEntity.ok(tagService.updateTag(id, tagUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable @Min(1) int id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
