package co.ravn.ecommerce.repositories.auth;

import co.ravn.ecommerce.entities.auth.Person;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByEmail(String email);
}
