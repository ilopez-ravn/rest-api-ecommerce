package co.ravn.ecommerce.Services.Order;

import co.ravn.ecommerce.DTO.Response.Order.OrderResponse;
import co.ravn.ecommerce.Entities.Order.SaleOrder;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Mappers.Order.OrderMapper;
import co.ravn.ecommerce.Mappers.Order.ShippingDetailsMapper;
import co.ravn.ecommerce.Repositories.Auth.UserRepository;
import co.ravn.ecommerce.Repositories.Cart.ShoppingCartRepository;
import co.ravn.ecommerce.Repositories.Clients.ClientAddressRepository;
import co.ravn.ecommerce.Repositories.Inventory.WarehouseRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryStatusRepository;
import co.ravn.ecommerce.Repositories.Order.DeliveryTrackingRepository;
import co.ravn.ecommerce.Repositories.Order.OrderBillRepository;
import co.ravn.ecommerce.Repositories.Order.OrderDetailsRepository;
import co.ravn.ecommerce.Repositories.Order.OrderTrackingLogRepository;
import co.ravn.ecommerce.Repositories.Order.SaleOrderRepository;
import co.ravn.ecommerce.Repositories.Order.StripePaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private OrderService orderService;

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
}
