package com.outpass.profile_service.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        System.out.println("========================================");
        System.out.println("INITIALIZING PROFILE SERVICE SECURITY");
        System.out.println("========================================");

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Swagger
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Student → Student + Admin
                .requestMatchers("/profiles/students/**")
                .hasAnyRole("STUDENT", "ADMIN")
                .requestMatchers("/internal/warden-duties/today").permitAll()
                // Warden → Warden + Admin
                .requestMatchers("/profiles/wardens/**")
                .hasAnyRole("WARDEN", "ADMIN")

                // Security → Security + Admin
                .requestMatchers("/profiles/security/**")
                .hasAnyRole("SECURITY", "ADMIN")
                
                .requestMatchers("/profiles/warden-duties/**")
                .hasRole("ADMIN")
                
                .anyRequest()
                .authenticated()
            )

            .oauth2ResourceServer(
                oauth2 -> oauth2
                    .jwt(jwt ->
                        jwt.jwtAuthenticationConverter(
                            jwtAuthenticationConverter()
                        )
                    )
            );

        System.out.println(
            "PROFILE SERVICE SECURITY CONFIGURATION CREATED"
        );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        System.out.println(
            "Creating JWT Decoder for Profile Service"
        );

        SecretKey key = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );

        System.out.println(
            "JWT decoder initialized successfully"
        );

        return NimbusJwtDecoder
            .withSecretKey(key)
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        System.out.println(
            "Creating JwtAuthenticationConverter"
        );

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
            this::extractAuthorities
        );

        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(
            Jwt jwt) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("PROFILE SERVICE - JWT PROCESSING");
        System.out.println("========================================");

        /*
         * JWT claims
         */
        System.out.println(
            "JWT Claims: " + jwt.getClaims()
        );

        /*
         * Subject
         */
        System.out.println(
            "JWT Subject (sub): " + jwt.getSubject()
        );

        /*
         * Email
         */
        String email =
            jwt.getClaimAsString("email");

        System.out.println(
            "JWT Email: " + email
        );

        /*
         * Role
         */
        String role =
            jwt.getClaimAsString("role");

        System.out.println(
            "JWT Role: " + role
        );

        /*
         * Validate role
         */
        if (role == null || role.isBlank()) {

            System.out.println(
                "WARNING: JWT DOES NOT CONTAIN A ROLE"
            );

            System.out.println(
                "No authorities will be granted"
            );

            System.out.println(
                "========================================"
            );

            return List.of();
        }

        /*
         * Create Spring Security authority
         */
        String authority =
            role.startsWith("ROLE_")
                ? role
                : "ROLE_" + role;

        System.out.println(
            "Generated Spring Security Authority: "
            + authority
        );

        /*
         * Final authority
         */
        List<GrantedAuthority> authorities =
            List.of(
                new SimpleGrantedAuthority(authority)
            );

        System.out.println(
            "Granted Authorities: " + authorities
        );

        System.out.println(
            "JWT AUTHENTICATION SUCCESSFUL"
        );

        System.out.println(
            "========================================"
        );

        return authorities;
    }
}