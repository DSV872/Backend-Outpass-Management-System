package com.outpass.auth_service.jwtservice;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.outpass.auth_service.config.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtProperties jwtProperties;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String userId, String email, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());
		return Jwts.builder().setSubject(userId).claim("userId", userId).claim("email", email).claim("role", role)
				.setIssuedAt(now).setExpiration(expiry).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}
}