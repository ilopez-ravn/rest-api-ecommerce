package co.ravn.ecommerce.Entities.Auth;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
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

    public SysUser() {
    }

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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdatedPassword() {
        return lastUpdatedPassword;
    }

    public void setLastUpdatedPassword(LocalDateTime lastUpdatedPassword) {
        this.lastUpdatedPassword = lastUpdatedPassword;
    }

    @Override
    public String toString() {
        return "SysUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", lastUpdatedPassword=" + lastUpdatedPassword +
                '}';
    }
}
