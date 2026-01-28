package co.ravn.ecommerce.Repositories;

import co.ravn.ecommerce.Entities.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SysUser, Integer> {
    Optional<SysUser> findByUsername(String username);
}
