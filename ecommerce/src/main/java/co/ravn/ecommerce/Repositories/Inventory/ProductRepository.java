package co.ravn.ecommerce.Repositories.Inventory;


import co.ravn.ecommerce.Entities.Inventory.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProductRepository extends PagingAndSortingRepository<Product, Integer>, JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByIdGreaterThanOrderByIdAsc(Long cursor, Pageable pageable);
    List<Product> findAllByOrderByIdAsc(Pageable pageable);
    boolean existsByIdGreaterThan(int id);



}
