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
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter implements GlobalFilter, Ordered {

	private final JwtService jwtService;

	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		String method = exchange.getRequest().getMethod().name();
		log.info("Gateway request: method={}, path={}", method, path);
		if (isPublicPath(path)) {
			log.debug("Public path, bypassing JWT validation: method={}, path={}", method, path);
			return chain.filter(exchange);
		}
		log.debug("Protected path, validating JWT: method={}, path={}", method, path);
		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (authHeader == null) {
			log.warn("Unauthorized request: Authorization header missing, method={}, path={}", method, path);
			return unauthorized(exchange);
		}
		if (!authHeader.startsWith("Bearer ")) {
			log.warn("Unauthorized request: invalid Authorization header format, method={}, path={}", method, path);
			return unauthorized(exchange);
		}
		String token = authHeader.substring(7);
		try {
			jwtService.validateToken(token);
			String email = jwtService.extractEmail(token);
			String role = jwtService.extractRole(token);
			log.debug("JWT validated successfully: role={}, path={}", role, path);
			boolean authorized = isAuthorized(role, path);
			if (!authorized) {
				log.warn("Forbidden request: role={}, method={}, path={}", role, method, path);
				return forbidden(exchange);
			}
			ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().header("X-User-Email", email)
					.header("X-User-Role", role).build();
			log.debug("Request authorized: role={}, method={}, path={}", role, method, path);
			return chain.filter(exchange.mutate().request(mutatedRequest).build());
		} catch (Exception ex) {
			log.warn("JWT validation failed: method={}, path={}, reason={}", method, path, ex.getMessage());
			return unauthorized(exchange);
		}
	}

	private boolean isPublicPath(String path) {
		return path.startsWith("/auth-service/swagger-ui") || path.startsWith("/auth-service/v3/api-docs")
				|| path.equals("/auth-service/auth/login") || path.equals("/auth-service/auth/refresh")
				|| path.equals("/auth-service/auth/register") || path.startsWith("/profile-service/swagger-ui")
				|| path.startsWith("/profile-service/v3/api-docs") || path.startsWith("/outpass-service/swagger-ui")
				|| path.startsWith("/outpass-service/v3/api-docs") || path.startsWith("/outpass-service/parent/")
				|| path.startsWith("/actuator");
	}

	private boolean isAuthorized(String role, String path) {
		if (role == null || role.isBlank()) {
			log.warn("Authorization failed: JWT role is missing, path={}", path);
			return false;
		}
		if ("STUDENT".equals(role) && path.startsWith("/outpass-service/student")) {
			return true;
		}
		if ("WARDEN".equals(role) && path.startsWith("/outpass-service/warden")) {
			return true;
		}
		if ("SECURITY".equals(role) && path.startsWith("/outpass-service/security")) {
			return true;
		}
		if (("STUDENT".equals(role) || "ADMIN".equals(role)) && path.startsWith("/profile-service/profiles/students")) {
			return true;
		}
		if (("WARDEN".equals(role) || "ADMIN".equals(role)) && path.startsWith("/profile-service/profiles/wardens")) {
			return true;
		}
		if (("SECURITY".equals(role) || "ADMIN".equals(role))
				&& path.startsWith("/profile-service/profiles/security")) {
			return true;
		}
		if ("ADMIN".equals(role) && path.startsWith("/profile-service/profiles/warden-duties")) {
			return true;
		}
		if ("ADMIN".equals(role) && path.startsWith("/auth-service/auth/users")) {
			return true;
		}
		log.debug("Authorization denied: role={}, path={}", role, path);
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