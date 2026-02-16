package co.ravn.ecommerce.Repositories.Auth;

import co.ravn.ecommerce.Entities.Auth.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SysUser, Integer> {
    Optional<SysUser> findByUsername(String username);
    List<SysUser> findByIsActiveTrue();

    // JOIN FETCH avoids the N+1 query problem by fetching the associated Person entity in the same query
    @Query("SELECT u FROM SysUser u JOIN FETCH u.person WHERE u.isActive = true")
    List<SysUser> findByIsActiveTrueWithPerson();


    Optional<SysUser> findById(int id);
}
