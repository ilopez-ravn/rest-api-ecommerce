package co.ravn.ecommerce.Repositories.Auth;

import co.ravn.ecommerce.Entities.Auth.Person;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByEmail(String email);
}
