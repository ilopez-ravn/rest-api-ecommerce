package co.ravn.ecommerce.dto.response.auth;

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
