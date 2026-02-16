package co.ravn.ecommerce.Entities.Inventory;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(name = "created_by")
    private int createdBy;

    private String description;

    @Column(name = "is_active")
    private boolean isActive;

    public Category() {
    }

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

    public Category(int id, String name, String description, int createdBy, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
