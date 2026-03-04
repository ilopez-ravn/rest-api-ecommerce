package co.ravn.ecommerce.repositories.inventory;

import co.ravn.ecommerce.entities.inventory.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TagRepository extends PagingAndSortingRepository<Tag, Integer>, JpaRepository<Tag, Integer> {
    List<Tag> findByIsActiveTrue();
    List<Tag> findAllByIdInAndIsActiveTrue(List<Integer> ids);
}
