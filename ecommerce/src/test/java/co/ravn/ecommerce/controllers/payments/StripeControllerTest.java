package co.ravn.ecommerce.controllers.payments;

import co.ravn.ecommerce.dto.request.payment.PaymentIntentRequest;
import co.ravn.ecommerce.dto.response.payment.PaymentIntentResponse;
import co.ravn.ecommerce.exception.GlobalExceptionHandler;
import co.ravn.ecommerce.exception.PaymentFailureException;
import co.ravn.ecommerce.exception.ResourceNotFoundException;
import co.ravn.ecommerce.filters.JwtAuthFilter;
import co.ravn.ecommerce.filters.RateLimitFilter;
import co.ravn.ecommerce.services.payments.StripePaymentService;
import co.ravn.ecommerce.services.payments.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StripeController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class StripeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StripePaymentService stripePaymentService;

    @MockitoBean
    private WebhookService webhookService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final String BASE_URL = "/api/v1/payments/stripe";

    @Nested
    @DisplayName("PUT /api/v1/payments/stripe/payment")
    class CreatePaymentIntent {

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 201 and client_secret when valid request")
        void createPaymentIntent_valid_returns201() throws Exception {
            String body = objectMapper.writeValueAsString(
                    new PaymentIntentRequest(1, 1, 1, new BigDecimal("5.00")));
            PaymentIntentResponse response = new PaymentIntentResponse("pi_secret_xxx");
            when(stripePaymentService.createOrRetrievePaymentIntent(any())).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.client_secret").value("pi_secret_xxx"));

            verify(stripePaymentService).createOrRetrievePaymentIntent(any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("forwards request body to service and returns 201 with client_secret")
        void createPaymentIntent_forwardsRequestToService() throws Exception {
            PaymentIntentRequest dto =
                    new PaymentIntentRequest(3, 10, 20, new BigDecimal("7.50"));
            String body = objectMapper.writeValueAsString(dto);
            PaymentIntentResponse response = new PaymentIntentResponse("pi_secret_cart_flow");
            when(stripePaymentService.createOrRetrievePaymentIntent(any())).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.client_secret").value("pi_secret_cart_flow"));

            verify(stripePaymentService).createOrRetrievePaymentIntent(argThat(req ->
                    req.getShoppingCartId().equals(3)
                            && req.getWarehouseId().equals(10)
                            && req.getAddressId().equals(20)
                            && req.getDeliveryFee().compareTo(new BigDecimal("7.50")) == 0
            ));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 400 when shopping_cart_id is missing")
        void createPaymentIntent_missingCartId_returns400() throws Exception {
            String body = objectMapper.writeValueAsString(
                    new PaymentIntentRequest(null, 1, 1, null));

            mockMvc.perform(put(BASE_URL + "/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(stripePaymentService, never()).createOrRetrievePaymentIntent(any());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        @DisplayName("returns 404 when cart or resource not found")
        void createPaymentIntent_notFound_returns404() throws Exception {
            String body = objectMapper.writeValueAsString(
                    new PaymentIntentRequest(999, 1, 1, null));
            when(stripePaymentService.createOrRetrievePaymentIntent(any()))
                    .thenThrow(new ResourceNotFoundException("Cart not found with id: 999"));

            mockMvc.perform(put(BASE_URL + "/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());

            verify(stripePaymentService).createOrRetrievePaymentIntent(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/stripe/webhook")
    class HandleWebhook {

        @Test
        @DisplayName("returns 200 when valid signature and payload")
        void webhook_validSignature_returns200() throws Exception {
            String payload = "{\"type\":\"payment_intent.succeeded\",\"data\":{}}";
            doNothing().when(webhookService).handleStripeEvent(eq(payload), any());

            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", "v1,valid-signature")
                            .content(payload))
                    .andExpect(status().isOk());

            verify(webhookService).handleStripeEvent(eq(payload), eq("v1,valid-signature"));
        }

        @Test
        @DisplayName("returns 400 when signature is invalid")
        void webhook_invalidSignature_returns400() throws Exception {
            String payload = "{\"type\":\"event\"}";
            doThrow(new SignatureVerificationException("Bad signature", "sig"))
                    .when(webhookService).handleStripeEvent(eq(payload), any());

            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", "invalid")
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid Stripe signature"));

            verify(webhookService).handleStripeEvent(eq(payload), eq("invalid"));
        }

        @Test
        @DisplayName("returns 400 when webhook processing throws")
        void webhook_processingError_returns400() throws Exception {
            String payload = "{\"type\":\"event\"}";
            doThrow(new PaymentFailureException("Processing failed"))
                    .when(webhookService).handleStripeEvent(eq(payload), any());

            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", "v1,sig")
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Webhook processing error"));

            verify(webhookService).handleStripeEvent(eq(payload), eq("v1,sig"));
        }
    }
}
