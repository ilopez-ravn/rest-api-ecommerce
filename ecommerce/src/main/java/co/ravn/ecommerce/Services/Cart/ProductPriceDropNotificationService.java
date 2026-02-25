package co.ravn.ecommerce.Services.Cart;

import co.ravn.ecommerce.DTO.EmailType;
import co.ravn.ecommerce.Entities.Auth.Person;
import co.ravn.ecommerce.Entities.Cart.ProductLiked;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Email;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Repositories.Cart.ProductLikedRepository;
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
import org.springframework.transaction.annotation.Transactional;

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

    private static final String CART_TEMPLATE_PATH = "classpath:templates/price-drop-notification-email.html";
    private static final String LIKED_TEMPLATE_PATH = "classpath:templates/price-drop-liked-email.html";
    private static final String CART_SUBJECT = "Price drop on an item in your cart";
    private static final String LIKED_SUBJECT = "Price drop on a product you liked";

    private final ShoppingCartDetailsRepository shoppingCartDetailsRepository;
    private final ProductLikedRepository productLikedRepository;
    private final MailService mailService;
    private final EmailRepository emailRepository;
    private final ResourceLoader resourceLoader;

    @Transactional
    public void notifyClientsOfPriceDrop(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        if (product == null || oldPrice == null || newPrice == null) {
            return;
        }

        if (newPrice.compareTo(oldPrice) >= 0) {
            return;
        }

        Set<String> notifiedEmails = new HashSet<>();
        String productName = product.getName() != null ? product.getName() : "Product";

        notifyCartClients(product, productName, oldPrice, newPrice, notifiedEmails);
        notifyLikedProductClients(product, productName, oldPrice, newPrice, notifiedEmails);
    }

    private void notifyCartClients(Product product, String productName, BigDecimal oldPrice, BigDecimal newPrice, Set<String> notifiedEmails) {
        List<ShoppingCartDetails> cartDetails = shoppingCartDetailsRepository
                .findByProductIdAndCart_Status(product.getId(), ShoppingCartStatusEnum.ACTIVE);

        if (cartDetails.isEmpty()) {
            return;
        }

        String templateContent;
        try {
            Resource resource = resourceLoader.getResource(CART_TEMPLATE_PATH);
            templateContent = new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            log.error("Failed to read cart price-drop notification template", e);
            return;
        }

        for (ShoppingCartDetails detail : cartDetails) {
            if (detail.getCart() == null || detail.getCart().getClient() == null) {
                continue;
            }

            detail.setPrice(newPrice);
            detail.setUpdatedAt(LocalDateTime.now());
            shoppingCartDetailsRepository.save(detail);

            Person client = detail.getCart().getClient();
            if (!client.isActive()) {
                continue;
            }

            String recipientEmail = client.getEmail();
            if (recipientEmail == null || recipientEmail.isBlank()) {
                continue;
            }

            if (!notifiedEmails.add(recipientEmail)) {
                continue;
            }

            String name = client.getFullName() != null ? client.getFullName() : "Customer";
            String body = templateContent
                    .replace("{{name}}", name)
                    .replace("{{product_name}}", productName)
                    .replace("{{old_price}}", oldPrice.toPlainString())
                    .replace("{{new_price}}", newPrice.toPlainString())
                    .replace("{{cart_link}}", "https://your-frontend-url.com/cart/" + detail.getCart().getId());

            sendAndLog(recipientEmail, CART_SUBJECT, body, client.getSysUser(), productName);
        }
    }

    private void notifyLikedProductClients(Product product, String productName, BigDecimal oldPrice, BigDecimal newPrice, Set<String> notifiedEmails) {
        List<ProductLiked> likedEntries = productLikedRepository
                .findActiveUnnotifiedByProductId(product.getId());

        if (likedEntries.isEmpty()) {
            return;
        }

        String templateContent;
        try {
            Resource resource = resourceLoader.getResource(LIKED_TEMPLATE_PATH);
            templateContent = new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            log.error("Failed to read liked price-drop notification template", e);
            return;
        }

        for (ProductLiked liked : likedEntries) {
            if (liked.getUser() == null || liked.getUser().getPerson() == null) {
                continue;
            }

            Person client = liked.getUser().getPerson();
            String recipientEmail = client.getEmail();
            if (recipientEmail == null || recipientEmail.isBlank()) {
                continue;
            }

            // skip if already notified via cart notification above
            if (!notifiedEmails.add(recipientEmail)) {
                liked.setHasBeenNotified(true);
                productLikedRepository.save(liked);
                continue;
            }

            String name = client.getFullName() != null ? client.getFullName() : "Customer";
            String body = templateContent
                    .replace("{{name}}", name)
                    .replace("{{product_name}}", productName)
                    .replace("{{old_price}}", oldPrice.toPlainString())
                    .replace("{{new_price}}", newPrice.toPlainString())
                    .replace("{{product_link}}", "https://your-frontend-url.com/products/" + product.getId());

            sendAndLog(recipientEmail, LIKED_SUBJECT, body, liked.getUser(), productName);

            liked.setHasBeenNotified(true);
            productLikedRepository.save(liked);
        }
    }

    private void sendAndLog(String recipientEmail, String subject, String body, co.ravn.ecommerce.Entities.Auth.SysUser sysUser, String productName) {
        Email email = new Email(recipientEmail, "", "", subject, body, EmailStatusEnum.SENT, EmailType.PRODUCT_LIKED_ALERT);
        if (sysUser != null) {
            email.setUser(sysUser);
        }
        try {
            mailService.sendHtml(recipientEmail, subject, body);
            emailRepository.save(email);
            log.info("Price drop notification sent to {} for product {}", recipientEmail, productName);
        } catch (MessagingException e) {
            log.error("Failed to send price drop email to {} for product {}", recipientEmail, productName, e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        } catch (Exception e) {
            log.error("Unexpected error sending price drop notification to {}", recipientEmail, e);
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
        }
    }
}
