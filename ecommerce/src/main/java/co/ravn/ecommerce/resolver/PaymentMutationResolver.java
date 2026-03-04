package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.graphql.CreatePaymentIntentInput;
import co.ravn.ecommerce.dto.request.payment.PaymentIntentRequest;
import co.ravn.ecommerce.dto.response.payment.PaymentIntentResponse;
import co.ravn.ecommerce.services.payments.StripePaymentService;
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

