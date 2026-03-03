package co.ravn.ecommerce.Services.Payments;

import co.ravn.ecommerce.Config.StripeConfig;
import co.ravn.ecommerce.DTO.LowStockNotificationEvent;
import co.ravn.ecommerce.DTO.OrderPaidEvent;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Entities.Inventory.Product;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Entities.Order.StripePayment;
import co.ravn.ecommerce.Entities.Order.StripePaymentEventLog;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartDetailsRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Repositories.Inventory.ProductStockRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryTrackingRepository;
import co.ravn.ecommerce.Repositories.Order.ProcessedStripeEventRepository;
import co.ravn.ecommerce.Repositories.Order.SaleOrderRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentEventLogRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentRepository;
import co.ravn.ecommerce.Services.Order.ShippingService;
import co.ravn.ecommerce.Utils.enums.ShoppingCartStatusEnum;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private StripeConfig stripeConfig;

    @Mock
    private ProcessedStripeEventRepository processedStripeEventRepository;

    @Mock
    private StripePaymentRepository stripePaymentRepository;

    @Mock
    private StripePaymentEventLogRepository stripePaymentEventLogRepository;

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ShoppingCartDetailsRepository shoppingCartDetailsRepository;

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private DeliveryTrackingRepository deliveryTrackingRepository;

    @Mock
    private ShippingService shippingService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private Sinks.Many<OrderPaidEvent> orderPaidSink;

    @InjectMocks
    private WebhookService webhookService;

    @Nested
    @DisplayName("handlePaymentSucceeded")
    class HandlePaymentSucceeded {

        @Test
        @DisplayName("does nothing when StripePayment is not found")
        void doesNothingWhenPaymentNotFound() {
            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_123");

            when(stripePaymentRepository.findByStripePaymentId("pi_123"))
                    .thenReturn(Optional.empty());

            webhookService.handlePaymentSucceeded(intent);

            verify(stripePaymentRepository).findByStripePaymentId("pi_123");
            verify(stripePaymentRepository, never()).save(any(StripePayment.class));
            verify(saleOrderRepository, never()).save(any(SaleOrder.class));
            verify(shoppingCartRepository, never()).save(any(ShoppingCart.class));
        }

        @Test
        @DisplayName("marks payment and order succeeded, deducts stock, logs event when stock sufficient")
        void marksSucceededAndDeductsStockWhenSufficient() {
            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_123");
            when(intent.toJson()).thenReturn("{}");

            // Prepare order, cart, warehouse
            Warehouse warehouse = new Warehouse();
            warehouse.setId(1);

            ShoppingCart cart = new ShoppingCart();
            cart.setId(5);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);

            SaleOrder order = new SaleOrder();
            order.setId(10);
            order.setWarehouse(warehouse);
            order.setShoppingCart(cart);

            StripePayment payment = new StripePayment();
            payment.setOrder(order);
            payment.setPaymentStatus("PENDING");

            when(stripePaymentRepository.findByStripePaymentId("pi_123"))
                    .thenReturn(Optional.of(payment));

            // Cart items
            Product product = new Product();
            product.setId(100);
            product.setName("Widget");

            ShoppingCartDetails item = new ShoppingCartDetails();
            item.setProduct(product);
            item.setQuantity(2);

            when(shoppingCartDetailsRepository.findByCartId(cart.getId()))
                    .thenReturn(List.of(item));

            // Stock sufficient in warehouse
            ProductStock stock = new ProductStock();
            stock.setQuantity(10);

            when(productStockRepository.findByWarehouseIdAndProductId(warehouse.getId(), product.getId()))
                    .thenReturn(Optional.of(stock));

            // Total stock after deduction remains above low-stock threshold
            ProductStock totalStockRecord = new ProductStock();
            totalStockRecord.setQuantity(8);
            when(productStockRepository.findByProductId(product.getId()))
                    .thenReturn(List.of(totalStockRecord));

            webhookService.handlePaymentSucceeded(intent);

            assertThat(payment.getPaymentStatus()).isEqualTo("SUCCEEDED");
            assertThat(order.getIsActive()).isTrue();
            assertThat(cart.getStatus()).isEqualTo(ShoppingCartStatusEnum.PROCESSED);
            assertThat(stock.getQuantity()).isEqualTo(8); // 10 - 2

            verify(stripePaymentRepository, atLeastOnce()).save(payment);
            verify(saleOrderRepository).save(order);
            verify(shoppingCartRepository).save(cart);
            verify(productStockRepository).save(stock);
            verify(stripePaymentEventLogRepository).save(any(StripePaymentEventLog.class));

            // No low-stock event should be published in this basic scenario
            verify(applicationEventPublisher, never()).publishEvent(any(LowStockNotificationEvent.class));
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed")
    class HandlePaymentFailed {

        @Test
        @DisplayName("does nothing when StripePayment is not found")
        void doesNothingWhenPaymentNotFound() {
            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_999");

            when(stripePaymentRepository.findByStripePaymentId("pi_999"))
                    .thenReturn(Optional.empty());

            webhookService.handlePaymentFailed(intent);

            verify(stripePaymentRepository).findByStripePaymentId("pi_999");
            verify(stripePaymentRepository, never()).save(any(StripePayment.class));
            verify(stripePaymentEventLogRepository, never()).save(any(StripePaymentEventLog.class));
        }

        @Test
        @DisplayName("marks payment FAILED and logs event when payment found")
        void marksFailedAndLogsEvent() {
            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_fail");
            when(intent.toJson()).thenReturn("{}");

            StripePayment payment = new StripePayment();
            payment.setPaymentStatus("PENDING");

            when(stripePaymentRepository.findByStripePaymentId("pi_fail"))
                    .thenReturn(Optional.of(payment));

            webhookService.handlePaymentFailed(intent);

            assertThat(payment.getPaymentStatus()).isEqualTo("FAILED");
            verify(stripePaymentRepository).save(payment);

            ArgumentCaptor<StripePaymentEventLog> logCaptor =
                    ArgumentCaptor.forClass(StripePaymentEventLog.class);
            verify(stripePaymentEventLogRepository).save(logCaptor.capture());

            StripePaymentEventLog savedLog = logCaptor.getValue();
            assertThat(savedLog.getPayment()).isEqualTo(payment);
            assertThat(savedLog.getStatus()).isEqualTo("FAILED");
            assertThat(savedLog.getEventType()).isEqualTo("payment_intent.payment_failed");
        }
    }
}

