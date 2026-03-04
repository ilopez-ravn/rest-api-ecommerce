package co.ravn.ecommerce.repositories.auth;

import co.ravn.ecommerce.entities.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
