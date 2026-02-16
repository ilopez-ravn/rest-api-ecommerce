package co.ravn.ecommerce.Repositories;

import co.ravn.ecommerce.Entities.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Integer> {
}
