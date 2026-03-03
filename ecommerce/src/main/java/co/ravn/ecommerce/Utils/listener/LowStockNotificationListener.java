package co.ravn.ecommerce.Utils.listener;

import co.ravn.ecommerce.DTO.EmailType;
import co.ravn.ecommerce.DTO.LowStockNotificationEvent;
import co.ravn.ecommerce.Entities.Cart.ProductLiked;
import co.ravn.ecommerce.Entities.Email;
import co.ravn.ecommerce.Repositories.Cart.ProductLikedRepository;
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
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class LowStockNotificationListener {

    private final ResourceLoader resourceLoader;
    private final ProductLikedRepository productLikedRepository;
    private final MailService mailService;
    private final EmailRepository emailRepository;

    @Async
    @EventListener
    public void handleLowStock(LowStockNotificationEvent event) {
        List<ProductLiked> toNotify = productLikedRepository
                .findByProductIdAndHasBeenNotifiedFalseOrHasBeenNotifiedIsNull(event.productId());
        if (toNotify.isEmpty()) {
            return;
        }

        String templateContent;
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/low-stock-notification-email.html");
            templateContent = new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            log.error("Failed to read low-stock notification template", e);
            return;
        }

        String subject = "Low stock: " + event.productName();
        String productLink = "https://your-frontend-url.com/products/" + event.productId();
        String productNameReplaced = templateContent
                .replace("{{product_name}}", event.productName())
                .replace("{{remaining_stock}}", String.valueOf(event.remainingStock()))
                .replace("{{product_link}}", productLink);

        for (ProductLiked productLiked : toNotify) {
            if (Boolean.TRUE.equals(productLiked.getHasBeenNotified())) {
                continue;
            }
            if (productLiked.getUser() == null || productLiked.getUser().getPerson() == null) {
                log.warn("ProductLiked id={} has no user or person, skipping", productLiked.getId());
                continue;
            }
            String recipientEmail = productLiked.getUser().getPerson().getEmail();
            if (recipientEmail == null || recipientEmail.isBlank()) {
                log.warn("ProductLiked id={} has no email for user, skipping", productLiked.getId());
                continue;
            }
            String recipientName = productLiked.getUser().getPerson().getFullName();
            if (recipientName == null) {
                recipientName = "";
            }
            String emailContent = productNameReplaced.replace("{{name}}", recipientName);

            try {
                mailService.sendHtml(recipientEmail, subject, emailContent);
                Email email = new Email(
                        recipientEmail,
                        "",
                        "",
                        subject,
                        emailContent,
                        EmailStatusEnum.SENT,
                        EmailType.PRODUCT_LIKED_ALERT
                );
                email.setUser(productLiked.getUser());
                emailRepository.save(email);
                productLiked.setHasBeenNotified(true);
                productLikedRepository.save(productLiked);
                log.info("Low-stock notification sent to {} for product {}", recipientEmail, event.productName());
            } catch (MessagingException e) {
                log.error("Failed to send low-stock email to {} for product {}", recipientEmail, event.productName(), e);
                Email email = new Email(
                        recipientEmail,
                        "",
                        "",
                        subject,
                        emailContent,
                        EmailStatusEnum.NOT_SENT,
                        EmailType.PRODUCT_LIKED_ALERT
                );
                email.setUser(productLiked.getUser());
                emailRepository.save(email);
            } catch (Exception e) {
                log.error("Unexpected error sending low-stock notification to {}", recipientEmail, e);
                Email email = new Email(
                        recipientEmail,
                        "",
                        "",
                        subject,
                        emailContent,
                        EmailStatusEnum.NOT_SENT,
                        EmailType.PRODUCT_LIKED_ALERT
                );
                email.setUser(productLiked.getUser());
                emailRepository.save(email);
            }
        }
    }
}
