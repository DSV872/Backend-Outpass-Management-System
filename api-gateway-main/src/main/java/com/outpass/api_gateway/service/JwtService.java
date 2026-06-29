package com.outpass.api_gateway.service;

import java.security.Key;

import org.springframework.stereotype.Service;

import com.outpass.api_gateway.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
	
	private final JwtProperties jwtProperties;
	
	private Key signingKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
	}
	
	public Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(signingKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	public void validateToken(String token) {
		extractAllClaims(token);
	}
	
	public String extractEmail(String token) {
		return extractAllClaims(token).getSubject();
	}
	
	public String extractRole(String token) {
		return extractAllClaims(token).get("role",String.class);
	}
}
