package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.TagCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.TagUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.TagResponse;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Mappers.Inventory.TagMapper;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.TagRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@AllArgsConstructor
@Validated
public class TagService {

    private final TagRepository tagRepository;
    private final ProductRepository productRepository;
    private final TagMapper tagMapper;

    public ResponseEntity<?> getAllTags() {
        List<Tag> tags = tagRepository.findByIsActiveTrue();
        List<TagResponse> response = tags.stream()
                .map(tagMapper::toResponse)
                .toList();

        return ResponseEntity.ok()
                .body(response);
    }

    @Transactional
    public ResponseEntity<?> createTag(@Valid TagCreateRequest tagCreateRequest) {
        Tag tag = tagMapper.toEntity(tagCreateRequest);

        Tag savedTag = tagRepository.save(tag);
        return ResponseEntity.ok()
                .body(tagMapper.toResponse(savedTag));
    }

    @Transactional
    public ResponseEntity<?> updateTag(@Min(1) int id, @Valid TagUpdateRequest tagUpdateRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        tagMapper.updateFromRequest(tagUpdateRequest, tag);
        Tag updatedTag = tagRepository.save(tag);
        return ResponseEntity.ok()
                .body(tagMapper.toResponse(updatedTag));
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
