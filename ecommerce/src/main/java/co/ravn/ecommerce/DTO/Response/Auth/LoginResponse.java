package co.ravn.ecommerce.DTO.Response.Auth;

import co.ravn.ecommerce.Utils.enums.RoleEnum;
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
