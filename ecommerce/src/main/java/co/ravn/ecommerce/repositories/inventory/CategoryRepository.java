package co.ravn.ecommerce.repositories.inventory;

import co.ravn.ecommerce.entities.inventory.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>, JpaRepository<Category, Integer> {
    List<Category> findByIsActiveTrue();
    List<Category> findAllByIdInAndIsActiveTrue(List<Integer> ids);
}
