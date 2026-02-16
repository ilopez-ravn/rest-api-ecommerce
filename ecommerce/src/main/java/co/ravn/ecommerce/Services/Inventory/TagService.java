package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.TagCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.TagUpdateRequest;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Repositories.Inventory.TagRepository;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public ResponseEntity<?> getAllTags() {
        List<Tag> tags = tagRepository.findByIsActiveTrue();

        return ResponseEntity.ok()
                .body(tags);
    }

    public ResponseEntity<?> createTag(TagCreateRequest tagCreateRequest) {
        Tag tag = new Tag(tagCreateRequest.getName());

        Tag savedTag = tagRepository.save(tag);
        return ResponseEntity.ok()
                .body(savedTag);
    }

    public ResponseEntity<?> updateTag(@Min(1) int id, TagUpdateRequest tagUpdateRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        tag.setName(tagUpdateRequest.getName());
        Tag updatedTag = tagRepository.save(tag);
        return ResponseEntity.ok()
                .body(updatedTag);
    }

    public ResponseEntity<?> deleteTag(@Min(1) int id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        tag.setActive(false);
        tagRepository.save(tag);
        return ResponseEntity.ok()
                .body("Tag deleted successfully");
    }
}
