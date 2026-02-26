package co.ravn.ecommerce.Services.Cart;

import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Repositories.Cart.ProductLikedRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartDetailsRepository;
import co.ravn.ecommerce.Repositories.EmailRepository;
import co.ravn.ecommerce.Services.MailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPriceDropNotificationServiceTest {

    @Mock
    private ShoppingCartDetailsRepository shoppingCartDetailsRepository;

    @Mock
    private ProductLikedRepository productLikedRepository;

    @Mock
    private MailService mailService;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private ProductPriceDropNotificationService productPriceDropNotificationService;

    @Nested
    @DisplayName("notifyClientsOfPriceDrop")
    class NotifyClientsOfPriceDrop {

        @Test
        @DisplayName("does nothing when product is null")
        void doesNothingWhenProductNull() {
            productPriceDropNotificationService.notifyClientsOfPriceDrop(null, BigDecimal.TEN, BigDecimal.ONE);

            verify(shoppingCartDetailsRepository, never()).findByProductIdAndCart_Status(anyInt(), any());
            verify(productLikedRepository, never()).findActiveUnnotifiedByProductId(anyInt());
        }

        @Test
        @DisplayName("does nothing when new price is not lower than old price")
        void doesNothingWhenPriceNotDropped() {
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");

            productPriceDropNotificationService.notifyClientsOfPriceDrop(product, BigDecimal.ONE, BigDecimal.TEN);
            productPriceDropNotificationService.notifyClientsOfPriceDrop(product, BigDecimal.TEN, BigDecimal.TEN);

            verify(shoppingCartDetailsRepository, never()).findByProductIdAndCart_Status(anyInt(), any());
            verify(productLikedRepository, never()).findActiveUnnotifiedByProductId(anyInt());
        }

        @Test
        @DisplayName("does nothing when old price is null")
        void doesNothingWhenOldPriceNull() {
            Product product = new Product();
            product.setId(1);
            productPriceDropNotificationService.notifyClientsOfPriceDrop(product, null, BigDecimal.ONE);
            verify(shoppingCartDetailsRepository, never()).findByProductIdAndCart_Status(anyInt(), any());
        }
    }
}
