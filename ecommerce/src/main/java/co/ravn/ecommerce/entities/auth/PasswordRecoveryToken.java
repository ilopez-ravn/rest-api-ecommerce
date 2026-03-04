package co.ravn.ecommerce.entities.auth;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "password_recovery_token")
public class PasswordRecoveryToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SysUser user;

    @Column(name = "recovery_token", unique = true, nullable = false)
    private String recoveryToken;

    @Column(name = "token_expiry", nullable = false)
    private Date tokenExpiry;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PasswordRecoveryToken(SysUser user, String recoveryToken, Date tokenExpiry) {
        this.user = user;
        this.recoveryToken = recoveryToken;
        this.tokenExpiry = tokenExpiry;
        this.createdAt = LocalDateTime.now();
    }
}
