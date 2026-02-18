package co.ravn.ecommerce.Entities.Inventory;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tag")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(name = "is_active")
    private Boolean isActive;

    public Tag(String name) {
        this.name = name;
    }
}
