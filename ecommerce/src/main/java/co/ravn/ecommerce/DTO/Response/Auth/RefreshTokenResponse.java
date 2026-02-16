package co.ravn.ecommerce.DTO.Response.Auth;

import lombok.Getter;

@Getter
public class RefreshTokenResponse {
    private final String accessToken;

    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

}
