package co.ravn.ecommerce.filters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Rate limit filter for specific paths (e.g. reset password). One token bucket per client IP.
 * When the bucket has no tokens, responds with 429 Too Many Requests.
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final List<String> limitedPaths;
    private final Supplier<Bucket> bucketSupplier;
    private final boolean enabled;
    private final Cache<String, Bucket> bucketCache;

    public RateLimitFilter(
            List<String> limitedPaths,
            Supplier<Bucket> bucketSupplier,
            boolean enabled,
            long cacheMaxSize,
            int cacheTtlMinutes
    ) {
        this.limitedPaths = limitedPaths;
        this.bucketSupplier = bucketSupplier;
        this.enabled = enabled;
        this.bucketCache = Caffeine.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterAccess(Duration.ofMinutes(cacheTtlMinutes))
                .build(key -> bucketSupplier.get());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) return true;
        String uri = request.getRequestURI();
        return limitedPaths.stream().noneMatch(uri::equals);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String clientKey = resolveClientKey(request);
        Bucket bucket = bucketCache.get(clientKey, k -> bucketSupplier.get());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Rate limit exceeded for client key: {}", clientKey);
        response.setStatus(HttpStatus.SC_TOO_MANY_REQUESTS); // Too Many Requests
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String path = request.getRequestURI().replace("\"", "\\\"");
        response.getWriter().write("{\"status\":" + HttpStatus.SC_TOO_MANY_REQUESTS + ",\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\",\"path\":\"" + path + "\"}");
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote != null ? remote : "unknown";
    }
}
