package com.outpass.profile_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignConfig {

	@Bean
	public RequestInterceptor requestInterceptor() {

		return requestTemplate -> {

			var authentication = SecurityContextHolder.getContext().getAuthentication();

			System.out.println("========== PROFILE FEIGN ==========");
			System.out.println("Authentication: " + authentication);

			if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {

				String token = jwtAuthentication.getToken().getTokenValue();

				System.out.println("JWT found. Forwarding Authorization header");

				requestTemplate.header("Authorization", "Bearer " + token);

			} else {

				System.out.println("WARNING: No JwtAuthenticationToken found");
			}

			System.out.println("===================================");
		};
	}
}