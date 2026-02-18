package co.ravn.ecommerce.Repositories.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.ravn.ecommerce.Entities.Inventory.ProductChangesLog;

@Repository
public interface ProductChangesLogRepository extends JpaRepository<ProductChangesLog, Integer> {
}
