package co.ravn.ecommerce.Utils.listener;

import co.ravn.ecommerce.DTO.EmailType;
import co.ravn.ecommerce.DTO.OrderConfirmationEvent;
import co.ravn.ecommerce.Entities.Email;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Repositories.EmailRepository;
import co.ravn.ecommerce.Services.MailService;
import co.ravn.ecommerce.Utils.enums.EmailStatusEnum;
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
 * Sends a simple order confirmation email after payment succeeds and stock is confirmed.
 */
@Component
@AllArgsConstructor
@Slf4j
public class OrderConfirmationListener {

    private static final String TEMPLATE_PATH = "classpath:templates/order-confirmation-email.html";
    private static final String SUBJECT = "Your order has been confirmed";

    private final ResourceLoader resourceLoader;
    private final MailService mailService;
    private final EmailRepository emailRepository;

    @Async
    @EventListener
    public void onOrderConfirmed(OrderConfirmationEvent event) {
        SaleOrder order = event.order();
        if (order == null || order.getClient() == null) {
            log.warn("OrderConfirmationListener: order or client is null, skipping confirmation email");
            return;
        }

        Person client = order.getClient();
        String recipientEmail = client.getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("OrderConfirmationListener: client has no email, skipping for order id {}", order.getId());
            return;
        }

        String name = client.getFullName() != null ? client.getFullName() : "Customer";
        String orderId = String.valueOf(order.getId());

        Email email = new Email(
                recipientEmail,
                "",
                "",
                SUBJECT,
                "",
                EmailStatusEnum.SENT,
                EmailType.ORDER_CONFIRMATION
        );

        try {
            Resource resource = resourceLoader.getResource(TEMPLATE_PATH);
            String body = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", name)
                    .replace("{{order_id}}", orderId);

            mailService.sendHtml(recipientEmail, SUBJECT, body);
            email.setBody(body);

            SysUser clientUser = client.getSysUser();
            if (clientUser != null) {
                email.setUser(clientUser);
            }
            emailRepository.save(email);
            log.info("Order confirmation email sent to {} for order {}", recipientEmail, orderId);
        } catch (IOException e) {
            log.error("OrderConfirmationListener: failed to load template for order {}", order.getId(), e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        } catch (MessagingException e) {
            log.error("OrderConfirmationListener: failed to send email to {} for order {}", recipientEmail, order.getId(), e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        } catch (Exception e) {
            log.error("OrderConfirmationListener: unexpected error for order {}", order.getId(), e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        }
    }
}

