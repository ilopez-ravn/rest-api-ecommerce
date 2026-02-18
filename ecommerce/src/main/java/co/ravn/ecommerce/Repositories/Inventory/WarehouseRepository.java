package co.ravn.ecommerce.Repositories.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    List<Warehouse> findByIsActiveTrue();
}
