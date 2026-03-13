package com.urlshortener.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * <p>For development purposes, this config:
 * <ul>
 *   <li>Permits all requests to the API (no auth required)</li>
 *   <li>Disables CSRF so Postman/curl POST requests work without tokens</li>
 *   <li>Allows the H2 console to load in an iframe (X-Frame-Options)</li>
 * </ul>
 *
 * <p><strong>For production:</strong> restrict access, enable CSRF for browser
 * clients, add authentication (JWT/OAuth2), and remove H2 console entirely.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API (enable for browser form submissions in prod)
            .csrf(AbstractHttpConfigurer::disable)

            // Allow H2 console to render inside an iframe
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            // Permit all requests (add auth here for production)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
