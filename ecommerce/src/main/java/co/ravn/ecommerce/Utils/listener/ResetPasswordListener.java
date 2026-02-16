package co.ravn.ecommerce.Utils.listener;

import co.ravn.ecommerce.DTO.EmailType;
import co.ravn.ecommerce.DTO.PasswordResetEvent;
import co.ravn.ecommerce.Entities.Email;
import co.ravn.ecommerce.Entities.Auth.PasswordRecoveryToken;
import co.ravn.ecommerce.Repositories.EmailRepository;
import co.ravn.ecommerce.Repositories.Auth.PasswordRecoveryTokenRepository;
import co.ravn.ecommerce.Services.MailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ResetPasswordListener {

    @Autowired
    private ResourceLoader resourceLoader;

    private final EmailRepository emailRepository;
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
    private final MailService mailService;

    @Autowired
    public ResetPasswordListener(MailService mailService, EmailRepository emailRepository, PasswordRecoveryTokenRepository passwordRecoveryTokenRepository) {
        this.mailService = mailService;
        this.emailRepository = emailRepository;
        this.passwordRecoveryTokenRepository = passwordRecoveryTokenRepository;
    }

    @EventListener
    public void handleResetPassword(PasswordResetEvent passwordResetEvent) {
        Email email = new Email(
                passwordResetEvent.email(),
                "",
                "",
                "Password reset link",
                "",
                "SENT",
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
            email.setStatus("NOT_SENT");
            emailRepository.save(email);
            log.error("Failed to read password reset template", e);
            throw new RuntimeException("Failed to read password reset template", e);
        } catch (MessagingException e) {
            email.setStatus("NOT_SENT");
            emailRepository.save(email);
            log.error("Failed to send password reset email to: {}", passwordResetEvent.email(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            email.setStatus("NOT_SENT");
            emailRepository.save(email);
            log.error("Unexpected error in password reset event listener", e);
            throw new RuntimeException("Unexpected error in password reset event listener", e);
        }
    }
}
