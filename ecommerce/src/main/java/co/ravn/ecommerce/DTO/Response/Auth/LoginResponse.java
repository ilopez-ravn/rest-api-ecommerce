package co.ravn.ecommerce.DTO.Response.Auth;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String access_token;
    private final String refresh_token;

    public LoginResponse(String access_token, String refresh_token) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "access_token='" + access_token + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                '}';
    }

}
