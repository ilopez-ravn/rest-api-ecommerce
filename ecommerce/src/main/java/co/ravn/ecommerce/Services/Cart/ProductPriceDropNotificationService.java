package co.ravn.ecommerce.Services.Cart;

import co.ravn.ecommerce.DTO.EmailType;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Email;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartDetailsRepository;
import co.ravn.ecommerce.Repositories.EmailRepository;
import co.ravn.ecommerce.Services.MailService;
import co.ravn.ecommerce.Utils.enums.EmailStatusEnum;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class ProductPriceDropNotificationService {

    private static final String TEMPLATE_PATH = "classpath:templates/price-drop-notification-email.html";
    private static final String SUBJECT_TEMPLATE = "Price drop on an item in your cart";

    private final ShoppingCartDetailsRepository shoppingCartDetailsRepository;
    private final MailService mailService;
    private final EmailRepository emailRepository;
    private final ResourceLoader resourceLoader;

    public void notifyClientsOfPriceDrop(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        if (product == null || oldPrice == null || newPrice == null) {
            return;
        }

        if (newPrice.compareTo(oldPrice) >= 0) {
            return;
        }

        List<ShoppingCartDetails> cartDetails = shoppingCartDetailsRepository
                .findByProductIdAndCart_Status(product.getId(), ShoppingCartStatusEnum.ACTIVE);

        if (cartDetails.isEmpty()) {
            return;
        }

        String templateContent;
        try {
            Resource resource = resourceLoader.getResource(TEMPLATE_PATH);
            templateContent = new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            log.error("Failed to read price-drop notification template", e);
            return;
        }

        Set<String> notifiedEmails = new HashSet<>();

        for (ShoppingCartDetails detail : cartDetails) {
            if (detail.getCart() == null || detail.getCart().getClient() == null) {
                continue;
            }

            detail.setPrice(newPrice);
            detail.setUpdatedAt(LocalDateTime.now());
            shoppingCartDetailsRepository.save(detail);
            
            Person client = detail.getCart().getClient();
            String recipientEmail = client.getEmail();
            if (recipientEmail == null || recipientEmail.isBlank()) {
                continue;
            }

            if (!notifiedEmails.add(recipientEmail)) {
                continue;
            }

            String name = client.getFullName() != null ? client.getFullName() : "Customer";
            String productName = product.getName() != null ? product.getName() : "Product";
            String subject = SUBJECT_TEMPLATE;

            String body = templateContent
                    .replace("{{name}}", name)
                    .replace("{{product_name}}", productName)
                    .replace("{{old_price}}", oldPrice.toPlainString())
                    .replace("{{new_price}}", newPrice.toPlainString())
                    .replace("{{cart_link}}", "https://your-frontend-url.com/cart/" + detail.getCart().getId());

            Email email = new Email(
                    recipientEmail,
                    "",
                    "",
                    subject,
                    body,
                    EmailStatusEnum.SENT,
                    EmailType.PRODUCT_LIKED_ALERT
            );

            try {
                mailService.sendHtml(recipientEmail, subject, body);
                if (client.getSysUser() != null) {
                    email.setUser(client.getSysUser());
                }
                emailRepository.save(email);
                log.info("Price drop notification sent to {} for product {}", recipientEmail, productName);
            } catch (MessagingException e) {
                log.error("Failed to send price drop email to {} for product {}", recipientEmail, productName, e);
                email.setStatus(EmailStatusEnum.NOT_SENT);
                if (client.getSysUser() != null) {
                    email.setUser(client.getSysUser());
                }
                emailRepository.save(email);
            } catch (Exception e) {
                log.error("Unexpected error sending price drop notification to {}", recipientEmail, e);
                email.setStatus(EmailStatusEnum.NOT_SENT);
                if (client.getSysUser() != null) {
                    email.setUser(client.getSysUser());
                }
                emailRepository.save(email);
            }
        }
    }
}
