package com.busgo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                // Admin-only endpoints (write operations)
                .requestMatchers(HttpMethod.POST, 
                    "/api/v1/seat-layouts/**",
                    "/api/v1/buses/**",
                    "/api/v1/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, 
                    "/api/v1/seat-layouts/**",
                    "/api/v1/buses/**",
                    "/api/v1/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, 
                    "/api/v1/seat-layouts/**",
                    "/api/v1/buses/**",
                    "/api/v1/routes/**").hasRole("ADMIN")
                // Any administrative endpoints under /api/v1/admin/** require ADMIN
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Allow read-only public access to listings/endpoints
                .anyRequest().permitAll()
            )
            // expect JWT resource server configuration already present in the app
            .oauth2ResourceServer(oauth -> oauth.jwt());

        return http.build();
    }
}
