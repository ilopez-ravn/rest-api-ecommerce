package co.ravn.ecommerce.DTO.Response.Auth;

import co.ravn.ecommerce.Entities.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
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

    public LoginResponse(String access_token, String refresh_token) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

}
