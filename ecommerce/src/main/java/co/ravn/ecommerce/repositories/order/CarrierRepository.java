package co.ravn.ecommerce.repositories.order;

import co.ravn.ecommerce.entities.order.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Integer> {
}
