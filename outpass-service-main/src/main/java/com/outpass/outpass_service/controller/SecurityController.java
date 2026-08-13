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
import com.outpass.outpass_service.service.SecuirtyService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/security")
@SecurityRequirement(name = "bearerAuth")
public class SecurityController {

	private final SecuirtyService securityService;

	@PutMapping("/scan-out/{qrToken}")
	public ResponseEntity<String> scanOut(Authentication authentication, @PathVariable String qrToken) {

		String email = authentication.getName();
		String role = getRole(authentication);

		securityService.approve(email, role, qrToken);

		return ResponseEntity.ok("Hooray! You can cross the gate");
	}

	@PutMapping("/scan-in/{qrToken}")
	public ResponseEntity<String> scanIn(Authentication authentication, @PathVariable String qrToken) {

		String email = authentication.getName();
		String role = getRole(authentication);

		securityService.scanIn(email, role, qrToken);

		return ResponseEntity.ok("Hooray! You can in the college");
	}

	@GetMapping("/history")
	public ResponseEntity<List<SecurityOutpassDto>> getSecurityHistory() {

		List<SecurityOutpassDto> outpasses = securityService.getSecurityHistory();

		return ResponseEntity.ok(outpasses);
	}

	private String getRole(Authentication authentication) {

		return authentication.getAuthorities().stream().findFirst().map(authority -> authority.getAuthority())
				.map(authority -> authority.replace("ROLE_", "")).orElseThrow();
	}
}