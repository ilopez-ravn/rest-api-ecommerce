package co.ravn.ecommerce.Services.Order;

import co.ravn.ecommerce.DTO.EmailType;
import co.ravn.ecommerce.Entities.Email;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Order.DeliveryStatus;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Repositories.EmailRepository;
import co.ravn.ecommerce.Services.MailService;
import co.ravn.ecommerce.Utils.enums.EmailStatusEnum;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
@Slf4j
public class DeliveryStatusEmailService {

    private static final String TEMPLATE_PATH = "classpath:templates/delivery-status-update-email.html";
    private static final String SUBJECT = "Delivery status update for your order";

    private final MailService mailService;
    private final EmailRepository emailRepository;
    private final ResourceLoader resourceLoader;

    /**
     * Sends a delivery status update email to the order's client and persists the email to email_log.
     * If the client has no email or send fails, still saves an Email record with NOT_SENT.
     */
    public void sendDeliveryStatusUpdateEmail(DeliveryTracking tracking, DeliveryStatus previousStatus, DeliveryStatus newStatus) {
        if (tracking == null || tracking.getOrder() == null) {
            log.warn("DeliveryStatusEmailService: tracking or order is null, skipping email");
            return;
        }

        Person client = tracking.getOrder().getClient();
        if (client == null) {
            log.warn("DeliveryStatusEmailService: order has no client, skipping email for order id {}", tracking.getOrder().getId());
            return;
        }

        String recipientEmail = client.getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("DeliveryStatusEmailService: client has no email, skipping for order id {}", tracking.getOrder().getId());
            return;
        }

        String name = client.getFullName() != null ? client.getFullName() : "Customer";
        String orderId = String.valueOf(tracking.getOrder().getId());
        String previousStatusName = previousStatus != null ? previousStatus.getName() : "—";
        String newStatusName = newStatus != null ? newStatus.getName() : "—";
        String trackingNumber = tracking.getTrackingNumber() != null ? tracking.getTrackingNumber() : "—";

        try {
            Resource resource = resourceLoader.getResource(TEMPLATE_PATH);
            String body = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", name)
                    .replace("{{order_id}}", orderId)
                    .replace("{{previous_status}}", previousStatusName)
                    .replace("{{new_status}}", newStatusName)
                    .replace("{{tracking_number}}", trackingNumber);

            mailService.sendHtml(recipientEmail, SUBJECT, body);

            Email email = new Email(recipientEmail, null, null, SUBJECT, body, EmailStatusEnum.SENT, EmailType.DELIVERY_STATUS_UPDATE);
            SysUser clientUser = client.getSysUser();
            if (clientUser != null) {
                email.setUser(clientUser);
            }
            emailRepository.save(email);
            log.info("Delivery status update email sent to {} for order {}", recipientEmail, orderId);
        } catch (IOException e) {
            log.error("Failed to load delivery status template or read content for order {}", tracking.getOrder().getId(), e);
            saveNotSentEmail(recipientEmail, client.getSysUser(), "Template error: " + e.getMessage());
        } catch (MessagingException e) {
            log.error("Failed to send delivery status email to {} for order {}", recipientEmail, tracking.getOrder().getId(), e);
            saveNotSentEmail(recipientEmail, client.getSysUser(), "Send failed: " + e.getMessage());
        }
    }

    private void saveNotSentEmail(String recipientEmail, SysUser user, String fallbackBody) {
        Email email = new Email(recipientEmail, null, null, SUBJECT, fallbackBody, EmailStatusEnum.NOT_SENT, EmailType.DELIVERY_STATUS_UPDATE);
        if (user != null) {
            email.setUser(user);
        }
        emailRepository.save(email);
    }
}
