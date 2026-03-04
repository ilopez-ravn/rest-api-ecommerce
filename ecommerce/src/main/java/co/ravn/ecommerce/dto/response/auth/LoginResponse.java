package co.ravn.ecommerce.dto.response.auth;

import co.ravn.ecommerce.utils.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String access_token;
    private String refresh_token;
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private RoleEnum role;
}
