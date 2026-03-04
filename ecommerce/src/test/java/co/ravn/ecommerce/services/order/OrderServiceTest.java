package co.ravn.ecommerce.services.order;

import co.ravn.ecommerce.dto.request.order.ShippingStatusUpdateRequest;
import co.ravn.ecommerce.dto.DeliveryStatusChangedEvent;
import co.ravn.ecommerce.dto.response.order.OrderResponse;
import co.ravn.ecommerce.dto.response.order.PaginatedOrderResponse;
import co.ravn.ecommerce.dto.response.order.ShippingDetailsResponse;
import co.ravn.ecommerce.entities.auth.Person;
import co.ravn.ecommerce.entities.auth.Role;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.entities.order.DeliveryStatus;
import co.ravn.ecommerce.entities.order.DeliveryTracking;
import co.ravn.ecommerce.entities.order.OrderTrackingLog;
import co.ravn.ecommerce.entities.order.SaleOrder;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.exception.BadRequestException;
import co.ravn.ecommerce.mappers.order.OrderMapper;
import co.ravn.ecommerce.mappers.order.ShippingDetailsMapper;
import co.ravn.ecommerce.repositories.auth.UserRepository;
import co.ravn.ecommerce.repositories.cart.ShoppingCartRepository;
import co.ravn.ecommerce.repositories.clients.ClientAddressRepository;
import co.ravn.ecommerce.repositories.inventory.WarehouseRepository;
import co.ravn.ecommerce.repositories.order.DeliveryStatusRepository;
import co.ravn.ecommerce.repositories.order.DeliveryTrackingRepository;
import co.ravn.ecommerce.repositories.order.OrderBillRepository;
import co.ravn.ecommerce.repositories.order.OrderDetailsRepository;
import co.ravn.ecommerce.repositories.order.OrderTrackingLogRepository;
import co.ravn.ecommerce.repositories.order.SaleOrderRepository;
import co.ravn.ecommerce.repositories.order.StripePaymentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ClientAddressRepository clientAddressRepository;

    @Mock
    private OrderDetailsRepository orderDetailsRepository;

    @Mock
    private OrderBillRepository orderBillRepository;

    @Mock
    private DeliveryTrackingRepository deliveryTrackingRepository;

    @Mock
    private DeliveryStatusRepository deliveryStatusRepository;

    @Mock
    private OrderTrackingLogRepository orderTrackingLogRepository;

    @Mock
    private StripePaymentRepository stripePaymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ShippingDetailsMapper shippingDetailsMapper;

    @Mock
    private DeliveryStatusEmailService deliveryStatusEmailService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private OrderService orderService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {

        @Test
        @DisplayName("returns order response when found")
        void returnsResponseWhenFound() {
            SaleOrder order = new SaleOrder();
            order.setId(1);
            OrderResponse response = new OrderResponse();
            response.setId(1);
            when(saleOrderRepository.findById(1)).thenReturn(Optional.of(order));
            when(orderBillRepository.findByOrderId(1)).thenReturn(Optional.empty());
            when(deliveryTrackingRepository.findByOrderId(1)).thenReturn(Optional.empty());
            when(stripePaymentRepository.findByOrderId(1)).thenReturn(Optional.empty());
            when(orderDetailsRepository.findByOrderId(1)).thenReturn(List.of());
            when(orderMapper.toResponse(eq(order), isNull(), isNull(), isNull(), isNull(), any(List.class)))
                    .thenReturn(response);

            OrderResponse result = orderService.getOrderById(1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(saleOrderRepository).findById(1);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when order not found")
        void throwsWhenNotFound() {
            when(saleOrderRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found");
            verify(orderMapper, never()).toResponse(any(), any(), any(), any(), any(), any(List.class));
        }
    }

    @Nested
    @DisplayName("getShippingDetails")
    class GetShippingDetails {

        @Test
        @DisplayName("returns mapped shipping details when tracking exists")
        void returnsShippingDetailsWhenFound() {
            int orderId = 1;
            SaleOrder order = new SaleOrder();
            order.setId(orderId);

            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setId(10);

            List<OrderTrackingLog> logs = List.of(new OrderTrackingLog());
            ShippingDetailsResponse response = new ShippingDetailsResponse();

            when(saleOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(deliveryTrackingRepository.findByOrderId(orderId)).thenReturn(Optional.of(tracking));
            when(orderTrackingLogRepository.findByDeliveryTrackingIdOrderByChangedAtDesc(tracking.getId()))
                    .thenReturn(logs);
            when(shippingDetailsMapper.toResponse(tracking, logs)).thenReturn(response);

            ShippingDetailsResponse result = orderService.getShippingDetails(orderId);

            assertThat(result).isSameAs(response);
            verify(saleOrderRepository).findById(orderId);
            verify(deliveryTrackingRepository).findByOrderId(orderId);
            verify(orderTrackingLogRepository).findByDeliveryTrackingIdOrderByChangedAtDesc(tracking.getId());
        }

        @Test
        @DisplayName("throws when order or tracking not found")
        void throwsWhenOrderOrTrackingMissing() {
            when(saleOrderRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getShippingDetails(99))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found");

            SaleOrder order = new SaleOrder();
            order.setId(5);
            when(saleOrderRepository.findById(5)).thenReturn(Optional.of(order));
            when(deliveryTrackingRepository.findByOrderId(5)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getShippingDetails(5))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shipping details not found");
        }
    }

    @Nested
    @DisplayName("updateShippingStatus")
    class UpdateShippingStatus {

        private SysUser setupAuthenticatedUser() {
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken("manager1", null));
            Person person = new Person();
            person.setId(10);
            Role role = new Role();
            role.setName(co.ravn.ecommerce.utils.enums.RoleEnum.MANAGER);
            SysUser user = new SysUser();
            user.setId(1);
            user.setUsername("manager1");
            user.setPerson(person);
            user.setRole(role);
            when(userRepository.findByUsernameAndIsActiveTrue("manager1"))
                    .thenReturn(Optional.of(user));
            return user;
        }

        @Test
        @DisplayName("updates tracking status, logs change, sends email and returns details")
        void updatesStatusAndReturnsDetails() {
            setupAuthenticatedUser();

            int orderId = 1;
            SaleOrder order = new SaleOrder();
            order.setId(orderId);

            DeliveryStatus previous = new DeliveryStatus();
            previous.setId(1);
            previous.setStepOrder(1);

            DeliveryStatus newStatus = new DeliveryStatus();
            newStatus.setId(2);
            newStatus.setStepOrder(2);

            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setId(10);
            tracking.setOrder(order);
            tracking.setStatus(previous);

            ShippingStatusUpdateRequest req = new ShippingStatusUpdateRequest();
            req.setStatusId(2);

            ShippingDetailsResponse response = new ShippingDetailsResponse();

            when(saleOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(deliveryTrackingRepository.findByOrderId(orderId)).thenReturn(Optional.of(tracking));
            when(deliveryStatusRepository.findById(2)).thenReturn(Optional.of(newStatus));
            when(orderTrackingLogRepository.findByDeliveryTrackingIdOrderByChangedAtDesc(tracking.getId()))
                    .thenReturn(List.of(new OrderTrackingLog()));
            when(shippingDetailsMapper.toResponse(eq(tracking), anyList())).thenReturn(response);

            ShippingDetailsResponse result = orderService.updateShippingStatus(orderId, req);

            assertThat(result).isSameAs(response);
            assertThat(tracking.getStatus()).isEqualTo(newStatus);
            assertThat(tracking.getUpdatedAt()).isNotNull();

            verify(orderTrackingLogRepository).save(any(OrderTrackingLog.class));
            verify(deliveryTrackingRepository).save(tracking);
            verify(applicationEventPublisher).publishEvent(any(DeliveryStatusChangedEvent.class));
        }

        @Test
        @DisplayName("throws BadRequestException when status stepOrder moves backwards")
        void throwsWhenStatusMovesBackwards() {
            setupAuthenticatedUser();

            int orderId = 1;
            SaleOrder order = new SaleOrder();
            order.setId(orderId);

            DeliveryStatus previous = new DeliveryStatus();
            previous.setId(2);
            previous.setStepOrder(2);

            DeliveryStatus newStatus = new DeliveryStatus();
            newStatus.setId(1);
            newStatus.setStepOrder(1);

            DeliveryTracking tracking = new DeliveryTracking();
            tracking.setId(10);
            tracking.setOrder(order);
            tracking.setStatus(previous);

            ShippingStatusUpdateRequest req = new ShippingStatusUpdateRequest();
            req.setStatusId(1);

            when(saleOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(deliveryTrackingRepository.findByOrderId(orderId)).thenReturn(Optional.of(tracking));
            when(deliveryStatusRepository.findById(1)).thenReturn(Optional.of(newStatus));

            assertThatThrownBy(() -> orderService.updateShippingStatus(orderId, req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cannot go backwards");

            verify(orderTrackingLogRepository, never()).save(any());
            verify(deliveryTrackingRepository, never()).save(any());
            verify(applicationEventPublisher, never())
                    .publishEvent(any(DeliveryStatusChangedEvent.class));
        }
    }

    @Nested
    @DisplayName("getOrders")
    class GetOrders {

        private SysUser setupUser(String username, co.ravn.ecommerce.utils.enums.RoleEnum roleEnum, Integer personId) {
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(username, null));
            SysUser user = new SysUser();
            user.setId(1);
            user.setUsername(username);
            Role role = new Role();
            role.setName(roleEnum);
            user.setRole(role);
            if (personId != null) {
                Person person = new Person();
                person.setId(personId);
                user.setPerson(person);
            }
            when(userRepository.findByUsernameAndIsActiveTrue(username))
                    .thenReturn(Optional.of(user));
            return user;
        }

        @Test
        @DisplayName("returns paginated orders for manager user with given clientId")
        void returnsPaginatedOrdersForManager() {
            setupUser("manager1", co.ravn.ecommerce.utils.enums.RoleEnum.MANAGER, null);

            SaleOrder order = new SaleOrder();
            order.setId(10);
            Page<SaleOrder> page = new PageImpl<>(List.of(order));

            OrderResponse mapped = new OrderResponse();
            mapped.setId(10);

            when(saleOrderRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(orderBillRepository.findByOrderId(10)).thenReturn(Optional.empty());
            when(deliveryTrackingRepository.findByOrderId(10)).thenReturn(Optional.empty());
            when(stripePaymentRepository.findByOrderId(10)).thenReturn(Optional.empty());
            when(orderDetailsRepository.findByOrderId(10)).thenReturn(List.of());
            when(orderMapper.toResponse(eq(order), isNull(), isNull(), isNull(), isNull(), anyList()))
                    .thenReturn(mapped);

            PaginatedOrderResponse result = orderService.getOrders(
                    99, // clientId (respected for manager)
                    null,
                    1,   // page
                    20,  // pageSize
                    null,
                    "asc",
                    null,
                    null
            );

            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().getFirst().getId()).isEqualTo(10);
            assertThat(result.getPage_info().getCurrent_page()).isEqualTo(1);

            verify(saleOrderRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("throws AccessDeniedException when non-manager user has no person")
        void throwsAccessDeniedWhenNonManagerWithoutPerson() {
            setupUser("client1", co.ravn.ecommerce.utils.enums.RoleEnum.CLIENT, null);

            assertThatThrownBy(() -> orderService.getOrders(
                    null, null, 1, 10, null, "asc", null, null
            )).isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Access denied");

                    verify(saleOrderRepository, never())
                    .findAll(any(Specification.class), any(Pageable.class));
                        }
    }
}
