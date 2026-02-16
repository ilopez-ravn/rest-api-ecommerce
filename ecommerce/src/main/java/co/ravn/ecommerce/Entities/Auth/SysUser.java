package co.ravn.ecommerce.Entities.Auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sys_user")
public class SysUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "hashed_password")
    private String password;

    @OneToOne
    @JoinColumn(name="role_id", referencedColumnName = "id")
    private Role role;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToOne(mappedBy = "sysUser")
    private Person person;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated_password")
    private LocalDateTime lastUpdatedPassword;

    public SysUser(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }


    public SysUser(int id, String username, String password, Role role, boolean isActive, LocalDateTime createdAt, LocalDateTime lastUpdatedPassword) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastUpdatedPassword = lastUpdatedPassword;
    }

}
