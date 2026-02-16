package co.ravn.ecommerce.Entities.Auth;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "device_info")
    private String deviceInfo;

    public UserRefreshToken(SysUser user, String refreshToken, LocalDateTime tokenExpiry) {
        this.user = user;
        this.refreshToken = refreshToken;
        this.tokenExpiry = tokenExpiry;
    }

}
