package com.outpass.outpass_service.service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenService {
	private static final SecureRandom RANDOM = new SecureRandom();

	public String generateToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public String hash(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hashed);
		} catch (NoSuchAlgorithmException e) {
			log.error("SHA-256 algorithm is not available", e);
			throw new IllegalStateException("SHA-256 algorithm is not available", e);
		}
	}
}