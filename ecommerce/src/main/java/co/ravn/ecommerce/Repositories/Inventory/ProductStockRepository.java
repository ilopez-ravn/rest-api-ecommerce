package co.ravn.ecommerce.Repositories.Inventory;

import co.ravn.ecommerce.Entities.Inventory.ProductStock;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Integer> {
Optional<ProductStock> findByWarehouseIdAndProductId(int warehouseId, int productId);
}
