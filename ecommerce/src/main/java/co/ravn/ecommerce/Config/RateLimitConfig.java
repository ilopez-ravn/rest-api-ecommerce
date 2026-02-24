package co.ravn.ecommerce.Config;

import co.ravn.ecommerce.Filters.RateLimitFilter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Rate limiting for reset password endpoints only (per client IP, token bucket).
 */
@Configuration
public class RateLimitConfig {

    private static final List<String> PASSWORD_RESET_PATHS = List.of(
            "/api/v1/users/password/token",
            "/api/v1/users/password"
    );

    @Value("${rate-limit.password-reset.capacity:3}")
    private long passwordResetCapacity;

    @Value("${rate-limit.password-reset.refill-duration-minutes:15}")
    private long passwordResetRefillMinutes;

    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${rate-limit.cache-max-size:10000}")
    private long cacheMaxSize;

    @Value("${rate-limit.cache-ttl-minutes:60}")
    private int cacheTtlMinutes;

    @Bean
    public Supplier<Bucket> passwordResetBucketSupplier() {
        Bandwidth bandwidth = Bandwidth.simple(
                passwordResetCapacity,
                Duration.ofMinutes(passwordResetRefillMinutes)
        );
        return () -> Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    @Bean
    public RateLimitFilter rateLimitFilter(Supplier<Bucket> passwordResetBucketSupplier) {
        return new RateLimitFilter(
                PASSWORD_RESET_PATHS,
                passwordResetBucketSupplier,
                enabled,
                cacheMaxSize,
                cacheTtlMinutes
        );
    }
}
