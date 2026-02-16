package co.ravn.ecommerce.DTO.Request.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetRequest {
    private String password;
    private String token;
}
