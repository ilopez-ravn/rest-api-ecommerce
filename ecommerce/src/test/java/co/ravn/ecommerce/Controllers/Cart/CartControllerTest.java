package co.ravn.ecommerce.Controllers.Cart;

import co.ravn.ecommerce.DTO.Request.Cart.CartProductRequest;
import co.ravn.ecommerce.DTO.Request.Cart.NewCartRequest;
import co.ravn.ecommerce.DTO.Response.Cart.ShoppingCartResponse;
import co.ravn.ecommerce.Exception.BadRequestException;
import co.ravn.ecommerce.Exception.GlobalExceptionHandler;
import co.ravn.ecommerce.Exception.ResourceNotFoundException;
import co.ravn.ecommerce.Filters.JwtAuthFilter;
import co.ravn.ecommerce.Filters.RateLimitFilter;
import co.ravn.ecommerce.Services.Cart.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final String BASE_URL = "/api/v1/carts";

    @Nested
    @DisplayName("POST /api/v1/carts (create cart)")
    class CreateCart {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and cart when valid body")
        void createCart_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(
                    new NewCartRequest(List.of(new CartProductRequest(new BigDecimal("10.50"), 2, 1))));
            ShoppingCartResponse response = new ShoppingCartResponse();
            response.setId(1);
            response.setClient_id(100);
            response.setStatus("ACTIVE");
            response.setProducts(List.of());
            when(cartService.createCart(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.client_id").value(100))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(cartService).createCart(any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 400 when products is null or missing")
        void createCart_missingProducts_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new NewCartRequest(null));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(cartService, never()).createCart(any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("second create for same client returns existing cart (same id)")
        void createCart_secondRequestSameClient_returnsExistingCart() throws Exception {
            String body = objectMapper.writeValueAsString(
                    new NewCartRequest(List.of(new CartProductRequest(new BigDecimal("10"), 1, 1))));
            ShoppingCartResponse existingCart = new ShoppingCartResponse();
            existingCart.setId(1);
            existingCart.setClient_id(100);
            existingCart.setStatus("ACTIVE");
            existingCart.setProducts(List.of());
            when(cartService.createCart(any())).thenReturn(existingCart);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.client_id").value(100));

            verify(cartService).createCart(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/carts/{id}")
    class GetCartById {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and cart when exists")
        void getCartById_exists_returns200() throws Exception {
            ShoppingCartResponse response = new ShoppingCartResponse();
            response.setId(1);
            response.setClient_id(100);
            response.setStatus("ACTIVE");
            response.setProducts(List.of());
            when(cartService.getCartById(1)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(cartService).getCartById(1);
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 404 when cart not found")
        void getCartById_notFound_returns404() throws Exception {
            when(cartService.getCartById(999))
                    .thenThrow(new ResourceNotFoundException("Cart not found"));

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());

            verify(cartService).getCartById(999);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/carts/{id}/items (add item)")
    class AddItemToCart {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and cart when valid body")
        void addItem_valid_returns200() throws Exception {
            String body = objectMapper.writeValueAsString(new CartProductRequest(new BigDecimal("19.99"), 2, 1));
            ShoppingCartResponse response = new ShoppingCartResponse();
            response.setId(1);
            response.setClient_id(100);
            response.setStatus("ACTIVE");
            response.setProducts(List.of());
            when(cartService.addItemToCart(eq(1), any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL + "/1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(cartService).addItemToCart(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 404 when cart not found")
        void addItem_cartNotFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(new CartProductRequest(new BigDecimal("10"), 1, 1));
            when(cartService.addItemToCart(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Cart not found"));

            mockMvc.perform(post(BASE_URL + "/999/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(cartService).addItemToCart(eq(999), any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 400 when quantity is invalid")
        void addItem_invalidQuantity_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CartProductRequest(new BigDecimal("10"), 0, 1));

            mockMvc.perform(post(BASE_URL + "/1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));

            verify(cartService, never()).addItemToCart(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 400 when product has no stock")
        void addItem_productNoStock_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CartProductRequest(new BigDecimal("19.99"), 1, 1));
            when(cartService.addItemToCart(eq(1), any()))
                    .thenThrow(new BadRequestException("Insufficient stock for product id: 1"));

            mockMvc.perform(post(BASE_URL + "/1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Insufficient stock for product id: 1"));

            verify(cartService).addItemToCart(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 400 when requested quantity exceeds available stock")
        void addItem_quantityExceedsStock_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(new CartProductRequest(new BigDecimal("5.00"), 100, 2));
            when(cartService.addItemToCart(eq(1), any()))
                    .thenThrow(new BadRequestException("Insufficient stock for product id: 2"));

            mockMvc.perform(post(BASE_URL + "/1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(cartService).addItemToCart(eq(1), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/carts/{id}/items/{itemId} (remove item)")
    class RemoveItemFromCart {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 200 and cart when item removed")
        void removeItem_valid_returns200() throws Exception {
            ShoppingCartResponse response = new ShoppingCartResponse();
            response.setId(1);
            response.setClient_id(100);
            response.setStatus("ACTIVE");
            response.setProducts(List.of());
            when(cartService.removeItemFromCart(1, 10)).thenReturn(response);

            mockMvc.perform(delete(BASE_URL + "/1/items/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(cartService).removeItemFromCart(1, 10);
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 404 when cart or item not found")
        void removeItem_notFound_returns404() throws Exception {
            when(cartService.removeItemFromCart(999, 10))
                    .thenThrow(new ResourceNotFoundException("Cart item not found"));

            mockMvc.perform(delete(BASE_URL + "/999/items/10"))
                    .andExpect(status().isNotFound());

            verify(cartService).removeItemFromCart(999, 10);
        }
    }
}
