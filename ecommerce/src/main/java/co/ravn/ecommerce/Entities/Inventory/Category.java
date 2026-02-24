package co.ravn.ecommerce.Entities.Inventory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_by")
    private int createdBy;

    private String description;

    @Column(name = "is_active")
    private boolean isActive;


    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
    }

    public Category(String name, String description, int createdBy) {
        this.name = name;
        this.createdBy = createdBy;
        this.description = description;
        this.isActive = true;
    }

}
