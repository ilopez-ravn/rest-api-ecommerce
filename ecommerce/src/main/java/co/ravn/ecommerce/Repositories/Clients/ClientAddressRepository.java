package co.ravn.ecommerce.Repositories.Clients;

import co.ravn.ecommerce.Entities.Clients.ClientAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientAddressRepository extends JpaRepository<ClientAddress, Integer> {
    List<ClientAddress> findByClientId(int clientId);
}
