package co.ravn.ecommerce.Config;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.mail")
public class MailEnvValidation {
    @NotBlank
    private String host;

    @NotBlank
    private String port;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String propertiesMailSmtpStarttlsEnable;

}
