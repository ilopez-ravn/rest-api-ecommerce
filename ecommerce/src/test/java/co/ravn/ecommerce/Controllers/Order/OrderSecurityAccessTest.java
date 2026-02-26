package co.ravn.ecommerce.Controllers.Order;

import co.ravn.ecommerce.Config.TestSecurityConfig;
import co.ravn.ecommerce.Exception.GlobalExceptionHandler;
import co.ravn.ecommerce.Filters.JwtAuthFilter;
import co.ravn.ecommerce.Filters.RateLimitFilter;
import co.ravn.ecommerce.Services.Order.OrderService;
import co.ravn.ecommerce.Services.Payments.StripePaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles({"test", "orderSecurityTest"})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = true)
class OrderSecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("GET access for WAREHOUSE and SHIPPING")
    class GetOrdersAccess {

        @Test
        @WithMockUser(roles = "WAREHOUSE")
        @DisplayName("WAREHOUSE can view orders list")
        void warehouseCanViewOrders() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SHIPPING")
        @DisplayName("SHIPPING can view orders list")
        void shippingCanViewOrders() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Shipping status updates")
    class ShippingStatusUpdates {

        @Test
        @WithMockUser(roles = "WAREHOUSE")
        @DisplayName("WAREHOUSE can update shipping status")
        void warehouseCanUpdateShipping() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status_id\":2}"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SHIPPING")
        @DisplayName("SHIPPING can update shipping status")
        void shippingCanUpdateShipping() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status_id\":2}"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT cannot update shipping status")
        void clientCannotUpdateShipping() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/shipping")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status_id\":2}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Order deletion")
    class OrderDeletion {


        @Test
        @WithMockUser(roles = "WAREHOUSE")
        @DisplayName("WAREHOUSE cannot delete orders")
        void warehouseCannotDeleteOrder() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SHIPPING")
        @DisplayName("SHIPPING cannot delete orders")
        void shippingCannotDeleteOrder() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("CLIENT cannot delete orders")
        void clientCannotDeleteOrder() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }
    }
}

