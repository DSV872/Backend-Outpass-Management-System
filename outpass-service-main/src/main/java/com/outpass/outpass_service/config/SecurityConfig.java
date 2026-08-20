package com.outpass.outpass_service.config;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		System.out.println("==========================================");
		System.out.println("INITIALIZING OUTPASS SERVICE SECURITY");
		System.out.println("==========================================");

		http

				.csrf(csrf -> {
					System.out.println("CSRF: DISABLED");
					csrf.disable();
				})

				.sessionManagement(session -> {

					System.out.println("SESSION MANAGEMENT: STATELESS");

					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
				})

				.authorizeHttpRequests(auth -> {

					System.out.println("CONFIGURING AUTHORIZATION RULES");

					auth

							// Swagger
							.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

							// Actuator
							.requestMatchers("/actuator/health", "/actuator/info").permitAll()

							// Parent approval links
							.requestMatchers("/parent/**").permitAll()

							// Student
							.requestMatchers("/student/**").hasAnyRole("STUDENT", "ADMIN")

							// Warden
							.requestMatchers("/warden/**").hasAnyRole("WARDEN", "ADMIN")

							// Security
							.requestMatchers("/security/**").hasAnyRole("SECURITY", "ADMIN")

							.anyRequest().authenticated();
				})

				.oauth2ResourceServer(oauth2 -> {

					System.out.println("OAUTH2 RESOURCE SERVER: ENABLED");

					oauth2.jwt(jwt -> {

						System.out.println("JWT AUTHENTICATION CONVERTER CONFIGURED");

						jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
					});
				});

		SecurityFilterChain filterChain = http.build();

		System.out.println("OUTPASS SERVICE SECURITY CONFIGURATION READY");

		System.out.println("==========================================");

		return filterChain;
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {

		System.out.println("Creating Outpass JWT Authentication Converter");

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		converter.setJwtGrantedAuthoritiesConverter(jwt -> {

			System.out.println();
			System.out.println("========== OUTPASS JWT PROCESSING ==========");

			/*
			 * Print JWT claims
			 */
			System.out.println("JWT Claims: " + jwt.getClaims());

			/*
			 * Subject
			 */
			System.out.println("JWT Subject: " + jwt.getSubject());

			/*
			 * Email
			 */
			String email = jwt.getClaimAsString("email");

			System.out.println("JWT Email: " + email);

			/*
			 * Role
			 */
			String role = jwt.getClaimAsString("role");

			System.out.println("JWT Role: " + role);

			/*
			 * No role
			 */
			if (role == null || role.isBlank()) {

				System.out.println("WARNING: NO ROLE FOUND IN JWT");
				System.out.println("Granted Authorities: []");
				System.out.println("==========================================");

				return List.of();
			}

			/*
			 * Convert role into Spring authority
			 */
			String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

			System.out.println("Generated Authority: " + authority);

			/*
			 * IMPORTANT: Declare the collection using the parent interface GrantedAuthority
			 * instead of SimpleGrantedAuthority.
			 */
			Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

			System.out.println("Granted Authorities: " + authorities);

			System.out.println("JWT AUTHENTICATION SUCCESSFUL");

			System.out.println("==========================================");

			return authorities;
		});

		return converter;
	}


}