package co.ravn.ecommerce.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("orderSecurityTest")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/**")
                        .hasAnyRole("MANAGER", "CLIENT", "WAREHOUSE", "SHIPPING")
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/*/shipping")
                        .hasAnyRole("MANAGER", "WAREHOUSE", "SHIPPING")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/**")
                        .hasRole("MANAGER")
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}

