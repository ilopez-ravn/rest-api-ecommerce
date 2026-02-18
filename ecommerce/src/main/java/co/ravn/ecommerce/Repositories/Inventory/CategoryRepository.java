package co.ravn.ecommerce.Repositories.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>, JpaRepository<Category, Integer> {
    List<Category> findByIsActiveTrue();
    List<Category> findAllByIdInAndIsActiveTrue(List<Integer> ids);
}
