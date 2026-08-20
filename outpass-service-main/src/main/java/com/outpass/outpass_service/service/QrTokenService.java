package com.outpass.outpass_service.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QrTokenService {
	private final SecretKey secretKey;

	public QrTokenService(@Value("${security.qr-secret}") String qrSecret) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(qrSecret));
		log.info("QR token service initialized successfully");
	}

	public String generate(Long outpassId, String studentUserId, LocalDateTime expiresAt) {
		log.debug("Generating QR token: outpassId={}, expiresAt={}", outpassId, expiresAt);
		Date issuedAt = new Date();
		Date expiration = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant());
		String token = Jwts.builder().claim("outpassId", outpassId).claim("studentUserId", studentUserId)
				.setIssuedAt(issuedAt).setExpiration(expiration).signWith(secretKey).compact();
		log.info("QR token generated successfully: outpassId={}, studentUserId={}", outpassId, studentUserId);
		return token;
	}
}