package co.ravn.ecommerce.config;

import co.ravn.ecommerce.dto.OrderPaidEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * Provides a shared sink for order-paid events so GraphQL subscription
 * checkOrderPaymentStatus(shoppingCartId) can emit when Stripe webhook fires.
 */
@Configuration
public class OrderPaymentSubscriptionConfig {

    @Bean
    public Sinks.Many<OrderPaidEvent> orderPaidSink() {
        return Sinks.many().multicast().onBackpressureBuffer(256);
    }
}
