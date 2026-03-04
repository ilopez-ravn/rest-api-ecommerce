package co.ravn.ecommerce.services.inventory;

import co.ravn.ecommerce.dto.request.inventory.TagCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.TagUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.TagResponse;
import co.ravn.ecommerce.entities.inventory.Tag;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.mappers.inventory.TagMapper;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import co.ravn.ecommerce.repositories.inventory.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ProductRepository productRepository;
    private final TagMapper tagMapper;

    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagRepository.findByIsActiveTrue();
        return tags.stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    @Transactional
    public TagResponse createTag(TagCreateRequest tagCreateRequest) {
        Tag tag = tagMapper.toEntity(tagCreateRequest);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponse(savedTag);
    }

    @Transactional
    public TagResponse updateTag(int id, TagUpdateRequest tagUpdateRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        tagMapper.updateFromRequest(tagUpdateRequest, tag);
        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toResponse(updatedTag);
    }

    @Transactional
    public void deleteTag(int id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        tag.setIsActive(false);
        List<Product> products = productRepository.findAllByTags_Id(id);
        for (Product product : products) {
            if (product.getTags() != null) {
                product.getTags().removeIf(t -> t.getId() == id);
            }
        }
        productRepository.saveAll(products);
        tagRepository.save(tag);
    }
}
