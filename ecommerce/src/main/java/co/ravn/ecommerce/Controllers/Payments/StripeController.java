package co.ravn.ecommerce.Controllers.Payments;

import co.ravn.ecommerce.DTO.Request.Payment.PaymentIntentRequest;
import co.ravn.ecommerce.DTO.Response.Payment.PaymentIntentResponse;
import co.ravn.ecommerce.Services.Payments.StripePaymentService;
import co.ravn.ecommerce.Services.Payments.WebhookService;
import org.springframework.http.HttpStatus;
import com.stripe.exception.SignatureVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("api/v1/payments/stripe")
@AllArgsConstructor
@Validated
@Slf4j
public class StripeController {

    private final StripePaymentService stripePaymentService;
    private final WebhookService webhookService;

    @PutMapping("/payment")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@RequestBody @Valid PaymentIntentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stripePaymentService.createOrRetrievePaymentIntent(request));
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String stripeSignature) {
        try {
            String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            webhookService.handleStripeEvent(payload, stripeSignature);
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid Stripe signature");
        } catch (IOException e) {
            log.error("Failed to read webhook request body: {}", e.getMessage());
            return ResponseEntity.status(400).body("Failed to read request body");
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage());
            return ResponseEntity.status(400).body("Webhook processing error");
        }
    }
}
