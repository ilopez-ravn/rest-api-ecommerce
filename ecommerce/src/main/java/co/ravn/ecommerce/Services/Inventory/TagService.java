package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.TagCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.TagUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.TagResponse;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.TagRepository;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ProductRepository productRepository;

    @Autowired
    public TagService(TagRepository tagRepository, ProductRepository productRepository) {
        this.tagRepository = tagRepository;
        this.productRepository = productRepository;
    }

    public ResponseEntity<?> getAllTags() {
        List<Tag> tags = tagRepository.findByIsActiveTrue();
        List<TagResponse> response = tags.stream()
            .map(TagResponse::new)
            .toList();

        return ResponseEntity.ok()
            .body(response);
    }

    @Transactional
    public ResponseEntity<?> createTag(TagCreateRequest tagCreateRequest) {
        Tag tag = new Tag(tagCreateRequest.getName());

        Tag savedTag = tagRepository.save(tag);
        return ResponseEntity.ok()
                .body(new TagResponse(savedTag));
    }

    @Transactional
    public ResponseEntity<?> updateTag(@Min(1) int id, TagUpdateRequest tagUpdateRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        tag.setName(tagUpdateRequest.getName());
        Tag updatedTag = tagRepository.save(tag);
        return ResponseEntity.ok()
            .body(new TagResponse(updatedTag));
    }

    @Transactional
    public ResponseEntity<?> deleteTag(@Min(1) int id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        tag.setIsActive(false);
        List<Product> products = productRepository.findAllByTags_Id(id);
        for (Product product : products) {
            if (product.getTags() != null) {
                product.getTags().removeIf(t -> t.getId() == id);
            }
        }
        productRepository.saveAll(products);
        tagRepository.save(tag);
        return ResponseEntity.noContent().build();
    }
}
