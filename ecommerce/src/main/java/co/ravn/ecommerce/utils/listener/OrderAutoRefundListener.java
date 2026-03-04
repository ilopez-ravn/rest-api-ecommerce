package co.ravn.ecommerce.utils.listener;

import co.ravn.ecommerce.dto.EmailType;
import co.ravn.ecommerce.dto.OrderAutoRefundEvent;
import co.ravn.ecommerce.entities.Email;
import co.ravn.ecommerce.entities.auth.Person;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.order.SaleOrder;
import co.ravn.ecommerce.repositories.EmailRepository;
import co.ravn.ecommerce.services.MailService;
import co.ravn.ecommerce.utils.enums.EmailStatusEnum;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Sends an email when a payment succeeds but is immediately refunded
 * due to insufficient stock.
 */
@Component
@AllArgsConstructor
@Slf4j
public class OrderAutoRefundListener {

    private static final String TEMPLATE_PATH = "classpath:templates/payment-auto-refund-email.html";
    private static final String SUBJECT = "Your payment was refunded due to stock issues";

    private final ResourceLoader resourceLoader;
    private final MailService mailService;
    private final EmailRepository emailRepository;

    @Async
    @EventListener
    public void onOrderAutoRefunded(OrderAutoRefundEvent event) {
        SaleOrder order = event.order();
        if (order == null || order.getClient() == null) {
            log.warn("OrderAutoRefundListener: order or client is null, skipping auto-refund email");
            return;
        }

        Person client = order.getClient();
        String recipientEmail = client.getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("OrderAutoRefundListener: client has no email, skipping for order id {}", order.getId());
            return;
        }

        String name = client.getFullName() != null ? client.getFullName() : "Customer";
        String orderId = String.valueOf(order.getId());
        String reason = event.refundReason() != null ? event.refundReason() : "Insufficient stock";

        Email email = new Email(
                recipientEmail,
                "",
                "",
                SUBJECT,
                "",
                EmailStatusEnum.SENT,
                EmailType.REFUND_PROCESSED
        );

        try {
            Resource resource = resourceLoader.getResource(TEMPLATE_PATH);
            String body = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", name)
                    .replace("{{order_id}}", orderId)
                    .replace("{{reason}}", reason);

            mailService.sendHtml(recipientEmail, SUBJECT, body);
            email.setBody(body);

            SysUser clientUser = client.getSysUser();
            if (clientUser != null) {
                email.setUser(clientUser);
            }
            emailRepository.save(email);
            log.info("Auto-refund email sent to {} for order {}", recipientEmail, orderId);
        } catch (IOException e) {
            log.error("OrderAutoRefundListener: failed to load template for order {}", order.getId(), e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        } catch (MessagingException e) {
            log.error("OrderAutoRefundListener: failed to send email to {} for order {}", recipientEmail, order.getId(), e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        } catch (Exception e) {
            log.error("OrderAutoRefundListener: unexpected error for order {}", order.getId(), e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        }
    }
}

