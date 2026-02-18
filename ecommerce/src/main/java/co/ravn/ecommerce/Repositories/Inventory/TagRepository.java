package co.ravn.ecommerce.Repositories.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TagRepository extends PagingAndSortingRepository<Tag, Integer>, JpaRepository<Tag, Integer> {
    List<Tag> findByIsActiveTrue();
    List<Tag> findAllByIdInAndIsActiveTrue(List<Integer> ids);
}
