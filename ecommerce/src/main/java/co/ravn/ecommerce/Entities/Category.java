package co.ravn.ecommerce.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @OneToOne
    @JoinColumn(name="created_by", referencedColumnName = "id")
    SysUser user;

    private String description;

    @Column(name = "is_active")
    private boolean isActive;

    public Category() {
    }

    public Category(String name, String description, SysUser sysUser) {
        this.name = name;
        this.user = sysUser;
        this.description = description;
        this.isActive = true;
    }

    public Category(int id, String name, String description, SysUser sysUser, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.user = sysUser;
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

    public SysUser getSysUser() {
        return user;
    }

    public void setSysUser(SysUser sysUser) {
        this.user = sysUser;
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
