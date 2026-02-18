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
@Table(name = "product_changes_log")
public class ProductChangesLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "change_description", nullable = false, columnDefinition = "TEXT")
    private String changeDescription;

    @ManyToOne
    @JoinColumn(name = "changed_by", referencedColumnName = "id")
    private SysUser changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
    
    public ProductChangesLog(Product product, String changeDescription, SysUser changedBy) {
        this.product = product;
        this.changeDescription = changeDescription;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
}
