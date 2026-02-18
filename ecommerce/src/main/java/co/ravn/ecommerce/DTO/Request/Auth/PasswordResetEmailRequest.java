package co.ravn.ecommerce.DTO.Request.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetEmailRequest {
    private String email;
}
