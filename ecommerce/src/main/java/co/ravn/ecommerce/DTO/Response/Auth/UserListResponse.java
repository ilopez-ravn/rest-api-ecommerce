package co.ravn.ecommerce.DTO.Response.Auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserListResponse {
    List<UserDetailsResponse> users;

}
