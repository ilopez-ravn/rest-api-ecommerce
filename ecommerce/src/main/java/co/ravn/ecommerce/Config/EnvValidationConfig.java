package co.ravn.ecommerce.Config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "app")
public class EnvValidationConfig {
	@NotBlank
	private String jwtSecret;

	@NotNull
	@Min(1)
	private Integer jwtExpirationMinutes;

	@NotNull
	@Min(1)
	private Integer refreshTokenExpirationDays;

	public String getJwtSecret() {
		return jwtSecret;
	}

	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	public Integer getJwtExpirationMinutes() {
		return jwtExpirationMinutes;
	}

	public void setJwtExpirationMinutes(Integer jwtExpirationMinutes) {
		this.jwtExpirationMinutes = jwtExpirationMinutes;
	}

	public Integer getRefreshTokenExpirationDays() {
		return refreshTokenExpirationDays;
	}

	public void setRefreshTokenExpirationDays(Integer refreshTokenExpirationDays) {
		this.refreshTokenExpirationDays = refreshTokenExpirationDays;
	}
}
