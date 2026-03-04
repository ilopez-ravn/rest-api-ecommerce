package co.ravn.ecommerce.entities.auth;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_refresh_token")
public class UserRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SysUser user;

    @Column(name = "refresh_token", unique = true, nullable = false)
    private String refreshToken;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "token_expiry", nullable = false)
    private LocalDateTime tokenExpiry;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UserRefreshToken(SysUser user, String refreshToken, LocalDateTime tokenExpiry) {
        this.user = user;
        this.refreshToken = refreshToken;
        this.tokenExpiry = tokenExpiry;
        this.createdAt = LocalDateTime.now();
    }

}
