package co.ravn.ecommerce.Services.Payments;

import co.ravn.ecommerce.Config.StripeConfig;
import co.ravn.ecommerce.DTO.LowStockNotificationEvent;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Order.ProcessedStripeEvent;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Entities.Order.StripePayment;
import co.ravn.ecommerce.Entities.Order.StripePaymentEventLog;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartDetailsRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductStockRepository;
import co.ravn.ecommerce.Repositories.Order.ProcessedStripeEventRepository;
import co.ravn.ecommerce.Repositories.Order.SaleOrderRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryTrackingRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentEventLogRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentRepository;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import co.ravn.ecommerce.Services.Order.ShippingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class WebhookService {

    private final StripeConfig stripeConfig;
    private final ProcessedStripeEventRepository processedStripeEventRepository;
    private final StripePaymentRepository stripePaymentRepository;
    private final StripePaymentEventLogRepository stripePaymentEventLogRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartDetailsRepository shoppingCartDetailsRepository;
    private final ProductStockRepository productStockRepository;
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final ShippingService shippingService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void handleStripeEvent(String payload, String stripeSignature) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, stripeSignature, stripeConfig.getWebhookSecret());

        if (processedStripeEventRepository.existsById(event.getId())) {
            log.info("Duplicate Stripe event ignored: {}", event.getId());
            return;
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> stripeObjectOpt = deserializer.getObject();
        if (stripeObjectOpt.isEmpty()) {
            // API version mismatch: event api_version may differ from Stripe.API_VERSION.
            // Fallback to deserializeUnsafe() so we can still handle payment_intent events.
            try {
                StripeObject obj = deserializer.deserializeUnsafe();
                stripeObjectOpt = Optional.of(obj);
            } catch (EventDataObjectDeserializationException e) {
                log.warn("Could not deserialize Stripe event data for event: {}, rawJson: {}",
                        event.getId(), e.getRawJson(), e);
                return;
            }
        }

        StripeObject dataObject = stripeObjectOpt.get();
        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSucceeded((PaymentIntent) dataObject);
            case "payment_intent.payment_failed" -> handlePaymentFailed((PaymentIntent) dataObject);
            case "payment_intent.created" ->
                    log.info("PaymentIntent created: {}", dataObject instanceof PaymentIntent ? ((PaymentIntent) dataObject).getId() : event.getId());
            default -> log.info("Unhandled Stripe event type: {}", event.getType());
        }

        processedStripeEventRepository.save(new ProcessedStripeEvent(event.getId()));
    }

    protected void handlePaymentSucceeded(PaymentIntent intent) {
        Optional<StripePayment> paymentOpt = stripePaymentRepository.findByStripePaymentId(intent.getId());
        if (paymentOpt.isEmpty()) {
            log.error("StripePayment not found for PaymentIntent id: {}", intent.getId());
            return;
        }

        StripePayment stripePayment = paymentOpt.get();
        stripePayment.setPaymentStatus("SUCCEEDED");
        stripePaymentRepository.save(stripePayment);

        SaleOrder order = stripePayment.getOrder();
        order.setIsActive(true);
        saleOrderRepository.save(order);
        
        if(order.getWarehouse() == null) {
            throw new RuntimeException("Warehouse not found for order id: " + order.getId());
        }

        
        ShoppingCart cart = order.getShoppingCart();
        List<ShoppingCartDetails> items = shoppingCartDetailsRepository.findByCartId(cart.getId());
        Set<Product> productsDeducted = new HashSet<>();

        for (ShoppingCartDetails item : items) {
                // Deduct from the specific warehouse assigned to the order
                Optional<ProductStock> stockOpt = productStockRepository
                        .findByWarehouseIdAndProductId(order.getWarehouse().getId(), item.getProduct().getId());
                if (stockOpt.isEmpty()) {
                    log.warn("No stock record found in warehouse id={} for product id={}",
                            order.getWarehouse().getId(), item.getProduct().getId());
                    continue;
                }
                ProductStock stock = stockOpt.get();
                int deduct = Math.min(stock.getQuantity(), item.getQuantity());
                stock.setQuantity(stock.getQuantity() - deduct);
                productStockRepository.save(stock);
                productsDeducted.add(item.getProduct());
                if (deduct < item.getQuantity()) {
                    log.warn("Insufficient stock in warehouse id={} for product id={}, shortage={}",
                            order.getWarehouse().getId(), item.getProduct().getId(),
                            item.getQuantity() - deduct);
                }
        }

        for (Product product : productsDeducted) {
            int totalStock = productStockRepository.findByProductId(product.getId()).stream()
                    .mapToInt(ProductStock::getQuantity)
                    .sum();
            if (totalStock < 3) {
                applicationEventPublisher.publishEvent(
                        new LowStockNotificationEvent(product.getId(), product.getName(), totalStock));
            }
        }

        cart.setStatus(ShoppingCartStatusEnum.PROCESSED);
        shoppingCartRepository.save(cart);

        stripePaymentEventLogRepository.save(
                new StripePaymentEventLog(stripePayment, "payment_intent.succeeded", "SUCCEEDED", intent.toJson()));
    }

    protected void handlePaymentFailed(PaymentIntent intent) {
        Optional<StripePayment> paymentOpt = stripePaymentRepository.findByStripePaymentId(intent.getId());
        if (paymentOpt.isEmpty()) {
            log.error("StripePayment not found for PaymentIntent id: {}", intent.getId());
            return;
        }

        StripePayment stripePayment = paymentOpt.get();
        stripePayment.setPaymentStatus("FAILED");
        stripePaymentRepository.save(stripePayment);

        stripePaymentEventLogRepository.save(
                new StripePaymentEventLog(stripePayment, "payment_intent.payment_failed", "FAILED", intent.toJson()));
    }
}
