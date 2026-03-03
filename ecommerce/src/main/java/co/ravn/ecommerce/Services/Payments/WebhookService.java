package co.ravn.ecommerce.Services.Payments;

import co.ravn.ecommerce.Config.StripeConfig;
import co.ravn.ecommerce.DTO.LowStockNotificationEvent;
import co.ravn.ecommerce.DTO.OrderAutoRefundEvent;
import co.ravn.ecommerce.DTO.OrderConfirmationEvent;
import co.ravn.ecommerce.DTO.OrderPaidEvent;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Order.ProcessedStripeEvent;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Entities.Order.StripePayment;
import co.ravn.ecommerce.Entities.Order.StripePaymentEventLog;
import co.ravn.ecommerce.Exception.PaymentFailureException;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartDetailsRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductStockRepository;
import co.ravn.ecommerce.Repositories.Order.ProcessedStripeEventRepository;
import co.ravn.ecommerce.Repositories.Order.SaleOrderRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentEventLogRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentRepository;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
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
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<OrderPaidEvent> orderPaidSink;

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

        if (order.getWarehouse() == null) {
            throw new ResourceNotFoundException("Warehouse not found for order id: " + order.getId());
        }

        ShoppingCart cart = order.getShoppingCart();
        List<ShoppingCartDetails> items = shoppingCartDetailsRepository.findByCartId(cart.getId());

        // Validate stock and create refund if needed
        boolean insufficientStock = false;
        StringBuilder reasonBuilder = new StringBuilder("Insufficient stock");
        for (ShoppingCartDetails item : items) {
            Optional<ProductStock> stockOpt = productStockRepository
                    .findByWarehouseIdAndProductId(order.getWarehouse().getId(), item.getProduct().getId());
            if (stockOpt.isEmpty()) {
                insufficientStock = true;
                reasonBuilder.append("; no stock record for product id=").append(item.getProduct().getId());
                break;
            }
            ProductStock stock = stockOpt.get();
            if (stock.getQuantity() < item.getQuantity()) {
                insufficientStock = true;
                reasonBuilder.append("; product id=").append(item.getProduct().getId())
                        .append(" shortage=").append(item.getQuantity() - stock.getQuantity());
                break;
            }
        }

        if (insufficientStock) {
            String refundReason = reasonBuilder.toString();
            log.warn("Insufficient stock for order id={}, refunding: {}", order.getId(), refundReason);
            try {
                Refund.create(RefundCreateParams.builder()
                        .setPaymentIntent(intent.getId())
                        .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                        .build());
            } catch (StripeException e) {
                log.error("Stripe refund failed for PaymentIntent id={}: {}", intent.getId(), e.getMessage());
                throw new PaymentFailureException("Refund failed: " + e.getMessage(), e);
            }
            stripePayment.setPaymentStatus("REFUNDED");
            stripePaymentRepository.save(stripePayment);
            order.setCancelledAt(LocalDateTime.now());
            order.setRefundReason(refundReason);
            order.setIsActive(false);
            saleOrderRepository.save(order);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);
            shoppingCartRepository.save(cart);
            stripePaymentEventLogRepository.save(
                    new StripePaymentEventLog(stripePayment, "refund.created", "REFUNDED", intent.toJson()));
            // Notify user that payment succeeded but was immediately refunded due to stock issues
            applicationEventPublisher.publishEvent(new OrderAutoRefundEvent(order, refundReason));
            return;
        }

        // Reduce product stock
        Set<Product> productsDeducted = new HashSet<>();
        for (ShoppingCartDetails item : items) {
            Optional<ProductStock> stockOpt = productStockRepository
                    .findByWarehouseIdAndProductId(order.getWarehouse().getId(), item.getProduct().getId());
            ProductStock stock = stockOpt.get();
            int deduct = Math.min(stock.getQuantity(), item.getQuantity());
            stock.setQuantity(stock.getQuantity() - deduct);
            productStockRepository.save(stock);
            productsDeducted.add(item.getProduct());
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

        // Notify client via email and GraphQL subscription that the order has been confirmed
        applicationEventPublisher.publishEvent(new OrderConfirmationEvent(order));
        orderPaidSink.tryEmitNext(new OrderPaidEvent(cart.getId(), order.getId()));
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
