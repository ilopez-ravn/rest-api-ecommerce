package co.ravn.ecommerce.Config;

import co.ravn.ecommerce.Filters.JwtAuthFilter;
import co.ravn.ecommerce.Filters.RateLimitFilter;
import co.ravn.ecommerce.Services.Auth.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@AllArgsConstructor
@Profile("!orderSecurityTest")
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // WebMvcConfigurer.super.addCorsMappings(registry);
                registry.addMapping("/api/v1/**").allowedOrigins("*");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers.xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(
                                cps -> cps.policyDirectives("script-src 'self'")));

        http
                // Disable CSRF because is not needed for stateless JWT
                .csrf(csrf -> csrf.disable())

                // Configure endpoint auth and public endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/refresh", "/api/v1/users/login", "/api/v1/users/password/token",
                                "/api/v1/users/password")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**",
                                "/api/v1/categories", "/api/v1/categories/**", "/api/v1/tags", "/api/v1/tags/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/stripe/webhook").permitAll()
                        .requestMatchers("/graphiql", "/graphiql/**").permitAll()
                        .requestMatchers("/graphql", "/graphql/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/**")
                        .hasAnyRole("MANAGER", "CLIENT", "WAREHOUSE", "SHIPPING")
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/*/shipping")
                        .hasAnyRole("MANAGER", "WAREHOUSE", "SHIPPING")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/products", "/api/v1/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories", "/api/v1/tags").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**", "/api/v1/tags/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**", "/api/v1/tags/**")
                        .hasRole("MANAGER")
                        .requestMatchers("/api/v1/carts/**", "/api/v1/clients/**",
                                "/api/v1/payments/stripe/payment")
                        .hasAnyRole("MANAGER", "CLIENT")
                        .anyRequest().authenticated())

                // Stateless session (required for JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Rate limit by IP first, then JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtAuthFilter.class);

        return http.build();
    }

    /*
     * Password encoder bean (uses BCrypt hashing)
     * Critical for secure password storage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    /*
     * Authentication manager bean
     * Required for programmatic authentication (e.g., in /generateToken)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
