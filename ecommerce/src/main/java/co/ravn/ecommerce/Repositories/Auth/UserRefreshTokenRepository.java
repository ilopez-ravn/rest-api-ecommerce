package co.ravn.ecommerce.Repositories.Auth;

import co.ravn.ecommerce.Entities.Auth.UserRefreshToken;
import jakarta.transaction.Transactional;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Integer> {
    Optional<UserRefreshToken> findByRefreshToken(String refreshToken);

    @Transactional
    void deleteByUserId(int userId);
}
