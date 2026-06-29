package com.outpass.api_gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.outpass.api_gateway.service.JwtService;

import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtFilter implements GlobalFilter,Ordered{
	private final JwtService jwtService;

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		
		//skip auth-service
		if(path.startsWith("/auth") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator")
				|| path.startsWith("/outpass-service/parent/")) {
			return chain.filter(exchange);
		}
		
		//read authorization header
		String authHeader  = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		
		if(authHeader == null || !authHeader.startsWith("Bearer ")) {
			return unauthorized(exchange);
		}
		
		String token = authHeader.substring(7);
		try {
			//validate token
			jwtService.validateToken(token);
			
			//extract user details
			String email = jwtService.extractEmail(token);
			String role = jwtService.extractRole(token);
			
			//role-based access
			if(!isAuthorized(role,path)) {
				return forbidden(exchange);
			}
			//Forward user context
			ServerHttpRequest mutatedRequest = exchange.getRequest()
					.mutate()
					.header("X-User-Email", email)
					.header("X-User-Role",role)
					.build();
			return chain.filter(exchange.mutate().request(mutatedRequest).build());
		}catch(Exception e) {
			return unauthorized(exchange);
		}
	}

	private Mono<Void> forbidden(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
		return exchange.getResponse().setComplete();
	}

	private boolean isAuthorized(String role, String path) {
		if(role.equals("STUDENT") && path.startsWith("/outpass-service/student")) {
			return true;
		}
		if(role.equals("WARDEN") && path.startsWith("/outpass-service/warden")) {
			return true;
		}
		if(role.equals("SECURITY") && path.startsWith("/outpass-service/security")) {
			return true;
		}
		return false;
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		
		return exchange.getResponse().setComplete();
	}
	
	
}
