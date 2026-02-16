package co.ravn.ecommerce.Repositories.Auth;

import co.ravn.ecommerce.Entities.Auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
