package co.ravn.ecommerce.repositories.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.ravn.ecommerce.entities.inventory.ProductChangesLog;

@Repository
public interface ProductChangesLogRepository extends JpaRepository<ProductChangesLog, Integer> {
}
