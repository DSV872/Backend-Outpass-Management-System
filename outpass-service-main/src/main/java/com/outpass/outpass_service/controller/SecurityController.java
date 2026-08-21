package com.outpass.outpass_service.controller;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.outpass_service.dto.SecurityOutpassDto;
import com.outpass.outpass_service.service.SecurityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/security")
public class SecurityController {

	private final SecurityService securityService;

	// ============================================================
	// SCAN OUT
	// ============================================================

	@PutMapping("/scan-out/{qrToken}")
	public ResponseEntity<String> scanOut(Authentication authentication, @PathVariable String qrToken) {

		String role = getRole(authentication);

		securityService.approve(role, qrToken);

		return ResponseEntity.ok("Hooray! You can cross the gate");
	}

	// ============================================================
	// SCAN IN
	// ============================================================

	@PutMapping("/scan-in/{qrToken}")
	public ResponseEntity<String> scanIn(Authentication authentication, @PathVariable String qrToken) {

		String role = getRole(authentication);

		securityService.scanIn(role, qrToken);

		return ResponseEntity.ok("Hooray! You can in the college");
	}

	// ============================================================
	// SECURITY HISTORY
	// ============================================================

	@GetMapping("/history")
	public ResponseEntity<List<SecurityOutpassDto>> getSecurityHistory(Authentication authentication) {

		String role = getRole(authentication);

		List<SecurityOutpassDto> outpasses = securityService.getSecurityHistory(role);

		return ResponseEntity.ok(outpasses);
	}

	// ============================================================
	// GET ROLE
	// ============================================================

	private String getRole(Authentication authentication) {

		return authentication.getAuthorities().stream().findFirst().map(authority -> authority.getAuthority())
				.map(authority -> authority.replace("ROLE_", ""))
				.orElseThrow(() -> new IllegalStateException("User role not found"));
	}
}