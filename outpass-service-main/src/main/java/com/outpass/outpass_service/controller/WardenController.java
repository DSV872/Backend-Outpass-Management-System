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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/warden")
@RequiredArgsConstructor
public class WardenController {

	private final WardenService wardenService;

	// ============================================================
	// APPROVE OUTPASS
	// ============================================================

	@PutMapping("/approve/{outpassId}")
	public ResponseEntity<QrResponse> approve(Authentication authentication, @PathVariable Long outpassId) {

		String wardenUserId = getUserId(authentication);

		String role = getRole(authentication);

		QrResponse qr = wardenService.approve(wardenUserId, role, outpassId);

		return ResponseEntity.ok(qr);
	}

	// ============================================================
	// REJECT OUTPASS
	// ============================================================

	@PutMapping("/reject/{outpassId}")
	public ResponseEntity<String> reject(Authentication authentication, @PathVariable Long outpassId) {

		String wardenUserId = getUserId(authentication);

		String role = getRole(authentication);

		String response = wardenService.reject(wardenUserId, role, outpassId);

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// PENDING OUTPASSES
	// ============================================================

	@GetMapping("/pending")
	public ResponseEntity<List<OutpassPendingResponse>> pendingOutpassList(Authentication authentication) {

		String role = getRole(authentication);
		String wardenUserId = authentication.getName();

		List<OutpassPendingResponse> response = wardenService.getPendingOutpassList(wardenUserId, role);

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// WARDEN HISTORY
	// ============================================================

	@GetMapping("/history")
	public ResponseEntity<List<WardenHistory>> wardenHistory(Authentication authentication) {

		String wardenUserId = getUserId(authentication);

		String role = getRole(authentication);

		List<WardenHistory> history = wardenService.getWardenApproveList(wardenUserId, role);

		return ResponseEntity.ok(history);
	}

	// ============================================================
	// GET USER ID
	// ============================================================

	private String getUserId(Authentication authentication) {

		try {

			return authentication.getName();

		} catch (NumberFormatException ex) {

			throw new IllegalStateException("Authenticated user ID is invalid");
		}
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