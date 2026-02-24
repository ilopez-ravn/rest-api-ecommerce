package co.ravn.ecommerce.Utils.listener;

import co.ravn.ecommerce.DTO.ProductPriceDropEvent;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Repositories.Inventory.ProductRepository;
import co.ravn.ecommerce.Services.Cart.ProductPriceDropNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class ProductPriceDropListener {

    private final ProductRepository productRepository;
    private final ProductPriceDropNotificationService notificationService;

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

