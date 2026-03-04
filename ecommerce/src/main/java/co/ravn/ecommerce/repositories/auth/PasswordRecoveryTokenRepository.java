package co.ravn.ecommerce.repositories.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import co.ravn.ecommerce.entities.auth.PasswordRecoveryToken;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, Integer> {
    Optional<PasswordRecoveryToken> findByRecoveryToken(String recoveryToken);

    // Remove tokens by user
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByUserId(int userId);
}
