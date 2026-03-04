package co.ravn.ecommerce.repositories.auth;

import co.ravn.ecommerce.entities.auth.UserRefreshToken;
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
