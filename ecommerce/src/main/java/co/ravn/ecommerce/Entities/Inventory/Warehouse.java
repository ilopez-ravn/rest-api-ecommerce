package co.ravn.ecommerce.Entities.Inventory;

import co.ravn.ecommerce.Entities.Auth.SysUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "warehouse")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private SysUser createdBy;

    private String name;
    private String location;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public Warehouse(SysUser createdBy, String name, String location, Boolean isActive) {
        this.createdBy = createdBy;
        this.name = name;
        this.location = location;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
}
