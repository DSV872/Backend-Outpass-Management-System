package com.outpass.outpass_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.outpass_service.dto.OutpassPendingResponse;
import com.outpass.outpass_service.dto.QrResponse;
import com.outpass.outpass_service.dto.WardenHistory;
import com.outpass.outpass_service.service.WardenService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/warden")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WardenController {

	private final WardenService wardenService;

	@PutMapping("/approve/{outpassId}")
	public ResponseEntity<QrResponse> approve(Authentication authentication, @PathVariable long outpassId) {

		String email = authentication.getName();
		String role = getRole(authentication);

		QrResponse qr = wardenService.approve(email, role, outpassId);

		return ResponseEntity.ok(qr);
	}

	@PutMapping("/reject/{outpassId}")
	public ResponseEntity<String> reject(Authentication authentication, @PathVariable long outpassId) {

		String email = authentication.getName();
		String role = getRole(authentication);

		wardenService.reject(email, role, outpassId);

		return ResponseEntity.ok("warden rejected successfully");
	}

	@GetMapping("/pending")
	public ResponseEntity<List<OutpassPendingResponse>> pendingOutpassList(Authentication authentication) {

		String role = getRole(authentication);

		List<OutpassPendingResponse> response = wardenService.getPendingOutpassList(role);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/history")
	public ResponseEntity<List<WardenHistory>> wardenHistory(Authentication authentication) {

		String email = authentication.getName();
		String role = getRole(authentication);

		List<WardenHistory> wardenApproveList = wardenService.getWardenApproveList(email, role);

		return ResponseEntity.ok(wardenApproveList);
	}

	private String getRole(Authentication authentication) {

		return authentication.getAuthorities().stream().findFirst().map(authority -> authority.getAuthority())
				.map(authority -> authority.replace("ROLE_", "")).orElseThrow();
	}
}