package co.ravn.ecommerce.repositories.inventory;

import co.ravn.ecommerce.entities.inventory.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    List<Warehouse> findByIsActiveTrue();
}
