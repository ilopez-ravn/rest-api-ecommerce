package co.ravn.ecommerce.services.payments;

import co.ravn.ecommerce.config.StripeConfig;
import co.ravn.ecommerce.dto.request.payment.PaymentIntentRequest;
import co.ravn.ecommerce.dto.response.order.OrderStatusResponse;
import co.ravn.ecommerce.dto.response.order.OrderResponse;
import co.ravn.ecommerce.dto.response.payment.PaymentIntentResponse;
import co.ravn.ecommerce.entities.auth.Person;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.cart.ShoppingCart;
import co.ravn.ecommerce.entities.cart.ShoppingCartDetails;
import co.ravn.ecommerce.entities.clients.ClientAddress;
import co.ravn.ecommerce.entities.inventory.Product;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import co.ravn.ecommerce.entities.inventory.Warehouse;
import co.ravn.ecommerce.entities.order.SaleOrder;
import co.ravn.ecommerce.entities.order.StripePayment;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.cart.ShoppingCartDetailsRepository;
import co.ravn.ecommerce.repositories.cart.ShoppingCartRepository;
import co.ravn.ecommerce.repositories.clients.ClientAddressRepository;
import co.ravn.ecommerce.repositories.inventory.ProductStockRepository;
import co.ravn.ecommerce.repositories.inventory.WarehouseRepository;
import co.ravn.ecommerce.repositories.order.*;
import co.ravn.ecommerce.services.order.OrderService;
import co.ravn.ecommerce.utils.enums.ShoppingCartStatusEnum;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private StripeConfig stripeConfig;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ShoppingCartDetailsRepository shoppingCartDetailsRepository;

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private StripePaymentRepository stripePaymentRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ClientAddressRepository clientAddressRepository;

    @Mock
    private DeliveryTrackingRepository deliveryTrackingRepository;

    @Mock
    private DeliveryStatusRepository deliveryStatusRepository;

    @Mock
    private OrderTrackingLogRepository orderTrackingLogRepository;

    @Mock
    private OrderBillRepository orderBillRepository;

    @Mock
    private OrderDetailsRepository orderDetailsRepository;

    @InjectMocks
    private StripePaymentService stripePaymentService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private SysUser buildAuthenticatedUser(int personId, String username) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(username, null));

        Person person = new Person();
        person.setId(personId);

        SysUser user = new SysUser();
        user.setId(10);
        user.setUsername(username);
        user.setPerson(person);

        when(userRepository.findByUsernameAndIsActiveTrue(username)).thenReturn(Optional.of(user));
        return user;
    }

    @Nested
    @DisplayName("createOrRetrievePaymentIntent")
    class CreateOrRetrievePaymentIntent {

        @Test
        @DisplayName("returns existing client secret when order and payment already exist")
        void returnsExistingClientSecretWhenOrderAndPaymentExist() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;
            int warehouseId = 2;
            int addressId = 3;

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            Person client = new Person();
            client.setId(5);
            cart.setClient(client);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);

            Warehouse warehouse = new Warehouse();
            warehouse.setId(warehouseId);

            ClientAddress address = new ClientAddress();
            address.setId(addressId);
            address.setClient(client);

            // Cart has at least one item
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");

            ProductStock stock = new ProductStock();
            stock.setProduct(product);
            stock.setQuantity(10);
            stock.setWarehouse(warehouse);
            
            product.setStock(List.of(stock));

            ShoppingCartDetails item =
                    ShoppingCartDetails.builder()
                    .cart(cart)
                    .product(product)
                    .price(BigDecimal.TEN)
                    .quantity(1)
                    .build();
        
                    cart.setProducts(List.of(item));

            SaleOrder existingOrder = new SaleOrder();
            existingOrder.setId(100);
            existingOrder.setClient(client);
            existingOrder.setShoppingCart(cart);
            existingOrder.setWarehouse(warehouse);

            StripePayment existingPayment = new StripePayment();
            existingPayment.setOrder(existingOrder);
            existingPayment.setClientSecretKey("pi_secret_existing");

            when(shoppingCartRepository.findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(clientAddressRepository.findById(addressId)).thenReturn(Optional.of(address));
            when(shoppingCartDetailsRepository.findByCartId(cartId))
                    .thenReturn(List.of(item));
            when(saleOrderRepository.findByShoppingCartId(cartId))
                    .thenReturn(Optional.of(existingOrder));
            when(stripePaymentRepository.findByOrderId(existingOrder.getId()))
                    .thenReturn(Optional.of(existingPayment));

            PaymentIntentRequest request = new PaymentIntentRequest(cartId, warehouseId, addressId, BigDecimal.ONE);

            PaymentIntentResponse response = stripePaymentService.createOrRetrievePaymentIntent(request);

            assertThat(response.getClient_secret()).isEqualTo("pi_secret_existing");
            verify(saleOrderRepository, never()).save(any(SaleOrder.class));
            verify(stripePaymentRepository, never()).save(any(StripePayment.class));
        }

        @Test
        @DisplayName("creates payment intent when order exists but no payment")
        void createsPaymentIntentWhenOrderHasNoExistingPayment() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;
            int warehouseId = 2;
            int addressId = 3;

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            Person client = new Person();
            client.setId(5);
            cart.setClient(client);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);

            Warehouse warehouse = new Warehouse();
            warehouse.setId(warehouseId);

            ClientAddress address = new ClientAddress();
            address.setId(addressId);
            address.setClient(client);

            // Cart has at least one item
            Product product = new Product();
            product.setId(1);
            product.setName("Widget");

            ProductStock stock = new ProductStock();
            stock.setProduct(product);
            stock.setQuantity(10);
            stock.setWarehouse(warehouse);
            
            product.setStock(List.of(stock));

            ShoppingCartDetails item = ShoppingCartDetails.builder()
                    .cart(cart)
                    .product(product)
                    .price(BigDecimal.TEN)
                    .quantity(2)
                    .build();

            cart.setProducts(List.of(item));

            SaleOrder existingOrder = new SaleOrder();
            existingOrder.setId(100);
            existingOrder.setClient(client);
            existingOrder.setShoppingCart(cart);
            existingOrder.setWarehouse(warehouse);

            when(shoppingCartRepository.findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(clientAddressRepository.findById(addressId)).thenReturn(Optional.of(address));
            when(shoppingCartDetailsRepository.findByCartId(cartId))
                    .thenReturn(List.of(item));
            when(saleOrderRepository.findByShoppingCartId(cartId))
                    .thenReturn(Optional.of(existingOrder));
            when(stripePaymentRepository.findByOrderId(existingOrder.getId()))
                    .thenReturn(Optional.empty());
            when(orderBillRepository.findByOrderId(existingOrder.getId()))
                    .thenReturn(Optional.empty());
            when(stripeConfig.getCurrency()).thenReturn("usd");

            PaymentIntentRequest request = new PaymentIntentRequest(cartId, warehouseId, addressId, BigDecimal.ONE);

            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_new");
            when(intent.getClientSecret()).thenReturn("pi_secret_new");

            try (MockedStatic<PaymentIntent> paymentIntentStatic = mockStatic(PaymentIntent.class)) {
                paymentIntentStatic
                        .when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(intent);

                PaymentIntentResponse response = stripePaymentService.createOrRetrievePaymentIntent(request);

                assertThat(response.getClient_secret()).isEqualTo("pi_secret_new");

                verify(stripePaymentRepository).save(argThat(saved ->
                        saved.getOrder() == existingOrder &&
                                "pi_new".equals(saved.getStripePaymentId()) &&
                                "pi_secret_new".equals(saved.getClientSecretKey()) &&
                                BigDecimal.valueOf(2).multiply(BigDecimal.TEN).add(BigDecimal.ONE)
                                        .compareTo(saved.getAmount()) == 0 &&
                                "usd".equals(saved.getCurrency())
                ));
            }
        }

        @Test
        @DisplayName("creates new order and payment intent when none exist and stock is sufficient")
        void createsOrderAndPaymentIntentWhenNoOrderExists() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;
            int warehouseId = 2;
            int addressId = 3;

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            Person client = new Person();
            client.setId(5);
            cart.setClient(client);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);

            Warehouse warehouse = new Warehouse();
            warehouse.setId(warehouseId);

            ClientAddress address = new ClientAddress();
            address.setId(addressId);
            address.setClient(client);

            Product product = new Product();
            product.setId(1);
            product.setName("Widget");
            product.setIsActive(true);

            ShoppingCartDetails item = ShoppingCartDetails.builder()
                    .cart(cart)
                    .product(product)
                    .price(BigDecimal.TEN)
                    .quantity(2)
                    .build();

            cart.setProducts(List.of(item));

            ProductStock stock = new ProductStock();
            stock.setProduct(product);
            stock.setQuantity(5);
            stock.setWarehouse(warehouse);
            product.setStock(List.of(stock));

            when(shoppingCartRepository.findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(clientAddressRepository.findById(addressId)).thenReturn(Optional.of(address));
            when(shoppingCartDetailsRepository.findByCartId(cartId))
                    .thenReturn(List.of(item));
            when(saleOrderRepository.findByShoppingCartId(cartId))
                    .thenReturn(Optional.empty());
            when(productStockRepository.findByWarehouseIdAndProductId(warehouseId, product.getId()))
                    .thenReturn(Optional.of(stock));

            // When saving a new order, assign an id so downstream logic works
            when(saleOrderRepository.save(any(SaleOrder.class))).thenAnswer(invocation -> {
                SaleOrder order = invocation.getArgument(0);
                order.setId(200);
                return order;
            });

            // Initial delivery status for tracking
            var initialStatus = new co.ravn.ecommerce.entities.order.DeliveryStatus();
            when(deliveryStatusRepository.findFirstByOrderByStepOrder())
                    .thenReturn(Optional.of(initialStatus));

            when(orderBillRepository.findByOrderId(200)).thenReturn(Optional.empty());
            when(stripeConfig.getCurrency()).thenReturn("usd");

            PaymentIntentRequest request =
                    new PaymentIntentRequest(cartId, warehouseId, addressId, BigDecimal.ONE);

            PaymentIntent intent = mock(PaymentIntent.class);
            when(intent.getId()).thenReturn("pi_fresh");
            when(intent.getClientSecret()).thenReturn("pi_secret_fresh");

            try (MockedStatic<PaymentIntent> paymentIntentStatic = mockStatic(PaymentIntent.class)) {
                paymentIntentStatic
                        .when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(intent);

                PaymentIntentResponse response = stripePaymentService.createOrRetrievePaymentIntent(request);

                assertThat(response.getClient_secret()).isEqualTo("pi_secret_fresh");

                // Verify a new order was created for the cart and warehouse
                verify(saleOrderRepository).save(argThat(savedOrder ->
                        savedOrder.getShoppingCart() == cart &&
                                savedOrder.getWarehouse() == warehouse &&
                                Boolean.FALSE.equals(savedOrder.getIsActive())
                ));

                // Verify a StripePayment was persisted for the newly created order
                verify(stripePaymentRepository).save(argThat(saved ->
                        saved.getOrder().getId() == 200 &&
                                "pi_fresh".equals(saved.getStripePaymentId()) &&
                                "pi_secret_fresh".equals(saved.getClientSecretKey())
                ));
            }
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when cart is not found")
        void throwsResourceNotFoundWhenCartMissing() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 99;
            PaymentIntentRequest request = new PaymentIntentRequest(cartId, 1, 1, null);

            when(shoppingCartRepository.findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> stripePaymentService.createOrRetrievePaymentIntent(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Active cart not found");

            verify(shoppingCartRepository).findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE);
            verifyNoMoreInteractions(warehouseRepository, clientAddressRepository, shoppingCartDetailsRepository);
        }

        @Test
        @DisplayName("throws AccessDeniedException when user does not own cart")
        void throwsAccessDeniedWhenUserDoesNotOwnCart() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;
            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            Person otherClient = new Person();
            otherClient.setId(10);
            cart.setClient(otherClient);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);

            when(shoppingCartRepository.findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));

            PaymentIntentRequest request = new PaymentIntentRequest(cartId, 1, 1, null);

            assertThatThrownBy(() -> stripePaymentService.createOrRetrievePaymentIntent(request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You do not own this cart");

            verify(shoppingCartRepository).findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE);
        }

        @Test
        @DisplayName("throws BadRequestException when cart is empty")
        void throwsBadRequestWhenCartEmpty() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;
            int warehouseId = 2;
            int addressId = 3;

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            Person client = new Person();
            client.setId(5);
            cart.setClient(client);
            cart.setStatus(ShoppingCartStatusEnum.ACTIVE);

            Warehouse warehouse = new Warehouse();
            warehouse.setId(warehouseId);

            ClientAddress address = new ClientAddress();
            address.setId(addressId);
            address.setClient(client);

            when(shoppingCartRepository.findByIdAndStatus(cartId, ShoppingCartStatusEnum.ACTIVE))
                    .thenReturn(Optional.of(cart));
            when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
            when(clientAddressRepository.findById(addressId)).thenReturn(Optional.of(address));
            when(shoppingCartDetailsRepository.findByCartId(cartId))
                    .thenReturn(List.of());

            PaymentIntentRequest request = new PaymentIntentRequest(cartId, warehouseId, addressId, null);

            assertThatThrownBy(() -> stripePaymentService.createOrRetrievePaymentIntent(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cart is empty");
        }
    }

    @Nested
    @MockitoSettings(strictness = Strictness.LENIENT)
    @DisplayName("getOrderStatusByShoppingCartId")
    class GetOrderStatusByShoppingCartId {

        @Test
        @DisplayName("returns COMPLETED when payment status is SUCCEEDED")
        void returnsCompletedWhenPaymentSucceeded() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;

            Person client = new Person();
            client.setId(5);

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            cart.setClient(client);

            SaleOrder order = new SaleOrder();
            order.setId(100);
            order.setClient(client);
            order.setShoppingCart(cart);

            StripePayment payment = new StripePayment();
            payment.setOrder(order);
            payment.setPaymentStatus("SUCCEEDED");

            lenient().when(saleOrderRepository.findByShoppingCartId(cartId)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));
            when(orderService.buildOrderResponse(order)).thenReturn(new OrderResponse());

            OrderStatusResponse response = stripePaymentService.getOrderStatusByShoppingCartId(cartId);

            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getOrder()).isNotNull();
        }

        @Test
        @DisplayName("returns REFUNDED and refund reason when order cancelled")
        void returnsRefundedWhenOrderCancelled() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;

            Person client = new Person();
            client.setId(5);

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            cart.setClient(client);

            SaleOrder order = new SaleOrder();
            order.setId(100);
            order.setClient(client);
            order.setShoppingCart(cart);
            order.setCancelledAt(LocalDateTime.now());
            order.setRefundReason("Out of stock");

            StripePayment payment = new StripePayment();
            payment.setOrder(order);
            payment.setPaymentStatus("PENDING");

            when(saleOrderRepository.findByShoppingCartId(cartId)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));
            when(orderService.buildOrderResponse(order)).thenReturn(new OrderResponse());

            OrderStatusResponse response = stripePaymentService.getOrderStatusByShoppingCartId(cartId);

            assertThat(response.getStatus()).isEqualTo("REFUNDED");
            assertThat(response.getRefund_reason()).isEqualTo("Out of stock");
        }

        @Test
        @DisplayName("returns PENDING when payment exists but not succeeded or refunded")
        void returnsPendingWhenPaymentPending() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;

            Person client = new Person();
            client.setId(5);

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            cart.setClient(client);

            SaleOrder order = new SaleOrder();
            order.setId(100);
            order.setClient(client);
            order.setShoppingCart(cart);

            StripePayment payment = new StripePayment();
            payment.setOrder(order);
            payment.setPaymentStatus("PENDING");

            when(saleOrderRepository.findByShoppingCartId(cartId)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));
            when(orderService.buildOrderResponse(order)).thenReturn(new OrderResponse());

            OrderStatusResponse response = stripePaymentService.getOrderStatusByShoppingCartId(cartId);

            assertThat(response.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("throws AccessDeniedException when user does not own order")
        void throwsAccessDeniedWhenUserDoesNotOwnOrder() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;

            Person client = new Person();
            client.setId(99); // different from authenticated person

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            cart.setClient(client);

            SaleOrder order = new SaleOrder();
            order.setId(100);
            order.setClient(client);
            order.setShoppingCart(cart);

            StripePayment payment = new StripePayment();
            payment.setOrder(order);
            payment.setPaymentStatus("SUCCEEDED");

            when(saleOrderRepository.findByShoppingCartId(cartId)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> stripePaymentService.getOrderStatusByShoppingCartId(cartId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You do not own this order");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when order not found")
        void throwsResourceNotFoundWhenOrderMissing() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 99;
            when(saleOrderRepository.findByShoppingCartId(cartId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stripePaymentService.getOrderStatusByShoppingCartId(cartId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when payment not found")
        void throwsResourceNotFoundWhenPaymentMissing() {
            buildAuthenticatedUser(5, "client1");

            int cartId = 1;

            Person client = new Person();
            client.setId(5);

            ShoppingCart cart = new ShoppingCart();
            cart.setId(cartId);
            cart.setClient(client);

            SaleOrder order = new SaleOrder();
            order.setId(100);
            order.setClient(client);
            order.setShoppingCart(cart);

            when(saleOrderRepository.findByShoppingCartId(cartId)).thenReturn(Optional.of(order));
            when(stripePaymentRepository.findByOrderId(order.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stripePaymentService.getOrderStatusByShoppingCartId(cartId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Payment not found");
        }
    }
}

