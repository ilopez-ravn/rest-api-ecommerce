package co.ravn.ecommerce.entities.auth;

import co.ravn.ecommerce.utils.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum name;

    @Column(name = "is_active")
    private Boolean isActive;

}
