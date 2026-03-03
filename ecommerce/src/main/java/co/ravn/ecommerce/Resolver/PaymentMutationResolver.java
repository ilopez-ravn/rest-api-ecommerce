package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.GraphQL.CreatePaymentIntentInput;
import co.ravn.ecommerce.DTO.Request.Payment.PaymentIntentRequest;
import co.ravn.ecommerce.DTO.Response.Payment.PaymentIntentResponse;
import co.ravn.ecommerce.Services.Payments.StripePaymentService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@AllArgsConstructor
public class PaymentMutationResolver {

    private final StripePaymentService stripePaymentService;

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public PaymentIntentResponse createPaymentIntent(@Argument("input") CreatePaymentIntentInput input) {
        BigDecimal deliveryFee = input.getDeliveryFee() != null ? BigDecimal.valueOf(input.getDeliveryFee()) : BigDecimal.ZERO;
        PaymentIntentRequest request = new PaymentIntentRequest(
                input.getShoppingCartId(),
                input.getWarehouseId(),
                input.getAddressId(),
                deliveryFee
        );
        return stripePaymentService.createOrRetrievePaymentIntent(request);
    }
}

