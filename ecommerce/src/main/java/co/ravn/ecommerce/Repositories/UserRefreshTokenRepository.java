package co.ravn.ecommerce.Repositories;

import co.ravn.ecommerce.Entities.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Integer> {
}
