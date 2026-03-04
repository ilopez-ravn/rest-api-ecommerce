package co.ravn.ecommerce.utils.listener;

import co.ravn.ecommerce.dto.EmailType;
import co.ravn.ecommerce.dto.PasswordResetEvent;
import co.ravn.ecommerce.entities.Email;
import co.ravn.ecommerce.entities.auth.PasswordRecoveryToken;
import co.ravn.ecommerce.exception.InternalServiceException;
import co.ravn.ecommerce.repositories.EmailRepository;
import co.ravn.ecommerce.repositories.auth.PasswordRecoveryTokenRepository;
import co.ravn.ecommerce.services.MailService;
import co.ravn.ecommerce.utils.enums.EmailStatusEnum;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;

@Component
@AllArgsConstructor
@Slf4j
public class ResetPasswordListener {

    private final ResourceLoader resourceLoader;
    private final EmailRepository emailRepository;
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
    private final MailService mailService;

    @Async
    @EventListener
    public void handleResetPassword(PasswordResetEvent passwordResetEvent) {
        Email email = new Email(
                passwordResetEvent.email(),
                "",
                "",
                "Password reset link",
                "",
                EmailStatusEnum.valueOf("SENT"),
                EmailType.valueOf("PASSWORD_RECOVERY")
        );
        try {
            log.info("Received password reset event for email: {}", passwordResetEvent.email());
            // load password template from resources/templates/password-reset-email.html
            Resource resource = resourceLoader.getResource("classpath:templates/password-reset-email.html");

            // replace {{reset_link}} with the actual reset link containing the token
            String resetLink = "https://your-frontend-url.com/reset-password?token=" + passwordResetEvent.token();

            // Read the template content once
            String emailContent = new String(resource.getInputStream().readAllBytes())
                    .replace("{{reset_link}}", resetLink)
                    .replace("{{name}}", passwordResetEvent.name());

            log.info("Password reset email sent to: {}", passwordResetEvent.email());
            email.setBody(emailContent);

            mailService.sendHtml(passwordResetEvent.email(), "Password reset link", emailContent);


            passwordRecoveryTokenRepository.save(new PasswordRecoveryToken(
                    passwordResetEvent.user(),
                    passwordResetEvent.token(),
                    passwordResetEvent.expiryDate()
            ));

            emailRepository.save(email);
        } catch (IOException e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Failed to read password reset template", e);
            throw new InternalServiceException("Failed to read password reset template", e);
        } catch (MessagingException e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Failed to send password reset email to: {}", passwordResetEvent.email(), e);
            throw new InternalServiceException("Failed to send password reset email", e);
        } catch (Exception e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Unexpected error in password reset event listener", e);
            throw new InternalServiceException("Unexpected error in password reset event listener", e);
        }
    }
}
