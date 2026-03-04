package co.ravn.ecommerce.controllers.order;

import co.ravn.ecommerce.dto.request.order.ShippingStatusUpdateRequest;
import co.ravn.ecommerce.dto.response.order.OrderResponse;
import co.ravn.ecommerce.dto.response.order.OrderStatusResponse;
import co.ravn.ecommerce.dto.response.order.PaginatedOrderResponse;
import co.ravn.ecommerce.dto.response.order.ShippingDetailsResponse;
import co.ravn.ecommerce.exception.GlobalExceptionHandler;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.filters.JwtAuthFilter;
import co.ravn.ecommerce.filters.RateLimitFilter;
import co.ravn.ecommerce.services.order.OrderService;
import co.ravn.ecommerce.services.payments.StripePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private StripePaymentService stripePaymentService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final String BASE_URL = "/api/v1/orders";

    @Nested
    @DisplayName("GET /api/v1/orders/status/{shoppingCartId}")
    class GetOrderStatus {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and order status when cart has order")
        void getOrderStatus_returns200() throws Exception {
            OrderStatusResponse response = new OrderStatusResponse();
            response.setStatus("PAID");
            response.setRefund_reason(null);
            when(stripePaymentService.getOrderStatusByShoppingCartId(1)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/status/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAID"));

            verify(stripePaymentService).getOrderStatusByShoppingCartId(1);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders")
    class GetOrders {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and paginated orders")
        void getOrders_returns200() throws Exception {
            PaginatedOrderResponse response = new PaginatedOrderResponse(
                    new PageImpl<>(List.of(), PageRequest.of(0, 10), 0),
                    List.of()
            );
            when(orderService.getOrders(any(), any(), eq(1), eq(10), any(), any(), any(), any()))
                    .thenReturn(response);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page_info.current_page").value(1))
                    .andExpect(jsonPath("$.page_info.page_size").value(10))
                    .andExpect(jsonPath("$.items.length()").value(0));

            verify(orderService).getOrders(isNull(), isNull(), eq(1), eq(10), isNull(), eq("desc"), isNull(), isNull());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 with client_id and status filters")
        void getOrders_withFilters_returns200() throws Exception {
            OrderResponse order = new OrderResponse();
            order.setId(100);
            PaginatedOrderResponse response = new PaginatedOrderResponse(
                    new PageImpl<>(List.of(), PageRequest.of(0, 5), 1),
                    List.of(order)
            );
            when(orderService.getOrders(eq(10), eq("PAID"), eq(1), eq(5), any(), any(), any(), any()))
                    .thenReturn(response);

            mockMvc.perform(get(BASE_URL)
                            .param("client_id", "10")
                            .param("status", "PAID")
                            .param("page", "1")
                            .param("page_size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].id").value(100));

            verify(orderService).getOrders(eq(10), eq("PAID"), eq(1), eq(5), isNull(), eq("desc"), isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId}")
    class GetOrderById {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and order when found")
        void getOrderById_returns200() throws Exception {
            OrderResponse response = new OrderResponse();
            response.setId(1);
            when(orderService.getOrderById(1)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(orderService).getOrderById(1);
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 404 when order not found")
        void getOrderById_notFound_returns404() throws Exception {
            when(orderService.getOrderById(999))
                    .thenThrow(new ResourceNotFoundException("Order not found with id: " + 999));

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(orderService).getOrderById(999);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/orders/{orderId}")
    class DeleteOrder {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 204 when order exists")
        void deleteOrder_returns204() throws Exception {
            doNothing().when(orderService).deleteOrder(1);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());

            verify(orderService).deleteOrder(1);
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when order not found")
        void deleteOrder_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Order not found with id: " + 999))
                    .when(orderService).deleteOrder(999);

            mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(orderService).deleteOrder(999);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId}/shipping")
    class GetShippingDetails {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and shipping details when found")
        void getShippingDetails_returns200() throws Exception {
            ShippingDetailsResponse response = new ShippingDetailsResponse();
            response.setDelivery_tracking(null);
            response.setHistory(List.of());
            response.setAddress(null);
            when(orderService.getShippingDetails(1)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/1/shipping"))
                    .andExpect(status().isOk());

            verify(orderService).getShippingDetails(1);
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 404 when order or shipping not found")
        void getShippingDetails_notFound_returns404() throws Exception {
            when(orderService.getShippingDetails(999))
                    .thenThrow(new ResourceNotFoundException("Shipping details not found for order: 999"));

            mockMvc.perform(get(BASE_URL + "/999/shipping"))
                    .andExpect(status().isNotFound());

            verify(orderService).getShippingDetails(999);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/orders/{orderId}/shipping")
    class UpdateShippingStatus {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 200 and shipping details when valid")
        void updateShippingStatus_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new ShippingStatusUpdateRequest(2));
            ShippingDetailsResponse response = new ShippingDetailsResponse();
            response.setDelivery_tracking(null);
            response.setHistory(List.of());
            response.setAddress(null);
            when(orderService.updateShippingStatus(eq(1), any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL + "/1/shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(orderService).updateShippingStatus(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 404 when order not found")
        void updateShippingStatus_orderNotFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(new ShippingStatusUpdateRequest(2));
            when(orderService.updateShippingStatus(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

            mockMvc.perform(post(BASE_URL + "/999/shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(orderService).updateShippingStatus(eq(999), any());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("returns 400 when status_id is missing")
        void updateShippingStatus_missingStatusId_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new ShippingStatusUpdateRequest(null));

            mockMvc.perform(post(BASE_URL + "/1/shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(orderService, never()).updateShippingStatus(eq(1), any());
        }
    }
}
