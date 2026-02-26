package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Request.Inventory.TagCreateRequest;
import co.ravn.ecommerce.DTO.Request.Inventory.TagUpdateRequest;
import co.ravn.ecommerce.DTO.Response.Inventory.TagResponse;
import co.ravn.ecommerce.Entities.Inventory.Tag;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Inventory.TagMapper;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Repositories.Inventory.TagRepository;
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
