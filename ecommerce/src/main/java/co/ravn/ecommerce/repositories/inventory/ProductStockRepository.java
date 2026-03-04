package co.ravn.ecommerce.repositories.inventory;

import co.ravn.ecommerce.entities.inventory.ProductStock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Integer> {
    Optional<ProductStock> findByWarehouseIdAndProductId(int warehouseId, int productId);
    List<ProductStock> findByProductId(int productId);
    List<ProductStock> findByWarehouseId(int warehouseId);
}
