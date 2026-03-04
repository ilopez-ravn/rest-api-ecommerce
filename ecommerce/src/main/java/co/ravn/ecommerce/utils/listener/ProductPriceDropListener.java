package co.ravn.ecommerce.utils.listener;

import co.ravn.ecommerce.dto.ProductPriceDropEvent;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.repositories.inventory.ProductRepository;
import co.ravn.ecommerce.services.cart.ProductPriceDropNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class ProductPriceDropListener {

    private final ProductRepository productRepository;
    private final ProductPriceDropNotificationService notificationService;

    @Async
    @EventListener
    public void handlePriceDrop(ProductPriceDropEvent event) {
        Optional<Product> productOpt = productRepository.findByIdAndDeletedAtIsNull(event.productId());
        if (productOpt.isEmpty()) {
            log.warn("ProductPriceDropListener: product not found for id {}", event.productId());
            return;
        }
        Product product = productOpt.get();
        notificationService.notifyClientsOfPriceDrop(product, event.oldPrice(), event.newPrice());
    }
}

