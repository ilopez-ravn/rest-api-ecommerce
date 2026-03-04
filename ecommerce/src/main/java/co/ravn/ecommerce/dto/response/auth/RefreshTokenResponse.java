package co.ravn.ecommerce.dto.response.auth;

import lombok.Getter;

@Getter
public class RefreshTokenResponse {
    private final String accessToken;

    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

}
