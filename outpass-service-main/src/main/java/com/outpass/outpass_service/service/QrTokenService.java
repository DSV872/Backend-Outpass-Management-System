package com.outpass.outpass_service.service;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;

@Service
public class QrTokenService {
	private static final String SECRET = "6hKvPsRSc+e7Ehkvt44ahqkU5f9ntYfAs2XdTU8d+QE=";
	
	public String generate(long outpassId, String email,LocalDateTime expiresAt) {
		Date expiryDate = java.sql.Timestamp.valueOf(expiresAt);
		return Jwts.builder()
				.setSubject("Outpass_QR")
				.claim("outpassId",outpassId)
				.claim("email",email)
				.setIssuedAt(new Date())
				.setExpiration(expiryDate)
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)),SignatureAlgorithm.HS256)
				.compact();
	}
}
