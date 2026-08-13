package com.outpass.api_gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.outpass.api_gateway.service.JwtService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtFilter implements GlobalFilter, Ordered {

	private final JwtService jwtService;

	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String path = exchange.getRequest().getURI().getPath();

		System.out.println("Gateway received: " + path);

		if (isPublicPath(path)) {
			System.out.println("PUBLIC PATH - bypassing JWT: " + path);
			return chain.filter(exchange);
		}

		System.out.println("PROTECTED PATH - checking JWT: " + path);

		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out.println("NO JWT - returning 401");
			return unauthorized(exchange);
		}

		String token = authHeader.substring(7);

		try {

			// Validate JWT
			jwtService.validateToken(token);

			// Extract user information
			String email = jwtService.extractEmail(token);
			String role = jwtService.extractRole(token);

			// Role-based authorization
			if (!isAuthorized(role, path)) {
				return forbidden(exchange);
			}

			// Forward authenticated user context
			ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().header("X-User-Email", email)
					.header("X-User-Role", role).build();

			return chain.filter(exchange.mutate().request(mutatedRequest).build());

		} catch (Exception e) {

			return unauthorized(exchange);
		}
	}

	private boolean isPublicPath(String path) {

	    return path.startsWith("/auth-service/swagger-ui")
	            || path.startsWith("/auth-service/v3/api-docs")

	            || path.equals("/auth-service/auth/login")
	            || path.equals("/auth-service/auth/refresh")

	            || path.startsWith("/outpass-service/swagger-ui")
	            || path.startsWith("/outpass-service/v3/api-docs")

	            || path.startsWith("/outpass-service/parent/")

	            || path.startsWith("/actuator");
	}

	private boolean isAuthorized(String role, String path) {

		if ("STUDENT".equals(role) && path.startsWith("/outpass-service/student")) {
			return true;
		}

		if ("WARDEN".equals(role) && path.startsWith("/outpass-service/warden")) {
			return true;
		}

		if ("SECURITY".equals(role) && path.startsWith("/outpass-service/security")) {
			return true;
		}

		return false;
	}

	private Mono<Void> forbidden(ServerWebExchange exchange) {

		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);

		return exchange.getResponse().setComplete();
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange) {

		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

		return exchange.getResponse().setComplete();
	}
}