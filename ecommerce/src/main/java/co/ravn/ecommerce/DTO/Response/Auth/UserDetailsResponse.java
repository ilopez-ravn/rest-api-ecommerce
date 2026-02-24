package co.ravn.ecommerce.DTO.Response.Auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDetailsResponse {
    private int id;
    private String username;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;
    private String person_id;
}
