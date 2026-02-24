package co.ravn.ecommerce.Repositories.Order;

import co.ravn.ecommerce.Entities.Order.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Integer> {
}
