package co.ravn.ecommerce.services.inventory;

import co.ravn.ecommerce.dto.request.inventory.TagCreateRequest;
import co.ravn.ecommerce.dto.request.inventory.TagUpdateRequest;
import co.ravn.ecommerce.dto.response.inventory.TagResponse;
import co.ravn.ecommerce.entities.inventory.Tag;
import co.ravn.ecommerce.mappers.inventory.TagMapper;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.repositories.inventory.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    @Nested
    @DisplayName("getAllTags")
    class GetAllTags {

        @Test
        @DisplayName("returns list of tag responses")
        void returnsList() {
            Tag tag = new Tag();
            tag.setId(1);
            tag.setName("sale");
            tag.setIsActive(true);
            TagResponse resp = new TagResponse();
            resp.setId(1);
            resp.setName("sale");
            when(tagRepository.findByIsActiveTrue()).thenReturn(List.of(tag));
            when(tagMapper.toResponse(tag)).thenReturn(resp);

            List<TagResponse> result = tagService.getAllTags();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("sale");
            verify(tagRepository).findByIsActiveTrue();
        }

        @Test
        @DisplayName("returns empty list when no tags")
        void returnsEmptyList() {
            when(tagRepository.findByIsActiveTrue()).thenReturn(List.of());

            List<TagResponse> result = tagService.getAllTags();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTag {

        @Test
        @DisplayName("saves and returns tag response")
        void createsTag() {
            TagCreateRequest request = new TagCreateRequest("featured");
            Tag entity = Tag.builder().id(1).name("featured").isActive(true).build();
            TagResponse response = new TagResponse();
            response.setId(1);
            response.setName("featured");

            when(tagMapper.toEntity(request)).thenReturn(entity);
            when(tagRepository.save(any(Tag.class))).thenReturn(entity);
            when(tagMapper.toResponse(entity)).thenReturn(response);

            TagResponse result = tagService.createTag(request);

            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("featured");
            verify(tagRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("updateTag")
    class UpdateTag {

        @Test
        @DisplayName("updates and returns tag when found")
        void updatesWhenFound() {
            Tag tag = Tag.builder().id(1).name("old").isActive(true).build();
            TagUpdateRequest request = new TagUpdateRequest("new");
            TagResponse response = new TagResponse();
            response.setId(1);
            response.setName("new");

            when(tagRepository.findById(1)).thenReturn(Optional.of(tag));
            doNothing().when(tagMapper).updateFromRequest(request, tag);
            when(tagRepository.save(tag)).thenReturn(tag);
            when(tagMapper.toResponse(tag)).thenReturn(response);

            TagResponse result = tagService.updateTag(1, request);

            assertThat(result.getName()).isEqualTo("new");
            verify(tagRepository).findById(1);
            verify(tagMapper).updateFromRequest(request, tag);
        }

        @Test
        @DisplayName("throws when tag not found")
        void throwsWhenNotFound() {
            when(tagRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tagService.updateTag(99, new TagUpdateRequest("x")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tag not found");
            verify(tagRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteTag")
    class DeleteTag {

        @Test
        @DisplayName("deactivates tag and removes from products when found")
        void deactivatesWhenFound() {
            Tag tag = new Tag();
            tag.setId(1);
            tag.setName("sale");
            tag.setIsActive(true);
            when(tagRepository.findById(1)).thenReturn(Optional.of(tag));
            when(productRepository.findAllByTags_Id(1)).thenReturn(List.of());
            when(tagRepository.save(any(Tag.class))).thenReturn(tag);

            tagService.deleteTag(1);

            verify(tagRepository).findById(1);
            assertThat(tag.getIsActive()).isFalse();
            verify(productRepository).findAllByTags_Id(1);
            verify(tagRepository).save(tag);
        }

        @Test
        @DisplayName("throws when tag not found")
        void throwsWhenNotFound() {
            when(tagRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tagService.deleteTag(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tag not found");
            verify(tagRepository, never()).save(any());
        }
    }
}
