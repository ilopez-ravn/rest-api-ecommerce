package co.ravn.ecommerce.repositories;

import co.ravn.ecommerce.entities.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Integer> {
}
