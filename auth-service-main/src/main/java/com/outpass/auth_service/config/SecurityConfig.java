package com.outpass.auth_service.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SecurityConfig {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
						.permitAll().requestMatchers("/error").permitAll().requestMatchers("/auth/login").permitAll()
						.requestMatchers("/auth/refresh").permitAll().requestMatchers("/auth/register").hasRole("ADMIN")
						.requestMatchers("/internal/users/**").permitAll().requestMatchers("/actuator/health")
						.permitAll().anyRequest().authenticated())
				.oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.httpBasic(httpBasic -> httpBasic.disable()).formLogin(form -> form.disable());
		log.info("Auth service security configuration initialized");
		return http.build();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		log.debug("Initializing JWT decoder");
		SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		return NimbusJwtDecoder.withSecretKey(key).build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
		return converter;
	}

	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		String userId = jwt.getSubject();
		String role = jwt.getClaimAsString("role");
		if (role == null || role.isBlank()) {
			log.warn("JWT authentication failed: role missing, userId={}", userId);
			return List.of();
		}
		String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
		log.debug("JWT authenticated: userId={}, role={}", userId, role);
		return List.of(new SimpleGrantedAuthority(authority));
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}