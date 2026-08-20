package com.outpass.outpass_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.outpass_service.dto.OutpassRequest;
import com.outpass.outpass_service.dto.OutpassResponse;
import com.outpass.outpass_service.dto.QrResponse;
import com.outpass.outpass_service.dto.ResendEmailRequest;
import com.outpass.outpass_service.dto.ResendEmailResponse;
import com.outpass.outpass_service.service.OutpassService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

	private final OutpassService outpassService;

	// ============================================================
	// APPLY OUTPASS
	// ============================================================

	@PostMapping("/apply")
	public ResponseEntity<String> apply(@Valid @RequestBody OutpassRequest outpassRequest,
			Authentication authentication) {

		String studentUserId = getUserId(authentication);

		String role = getRole(authentication);

		String token = outpassService.apply(outpassRequest, studentUserId, role);

		return ResponseEntity.ok("Outpass applied successfully");
	}

	// ============================================================
	// GET ONE OUTPASS
	// ============================================================

	@GetMapping("/outpasses/{outpassId}")
	public ResponseEntity<OutpassResponse> getOutpass(Authentication authentication, @PathVariable Long outpassId) {

		String studentUserId = getUserId(authentication);

		String role = getRole(authentication);

		OutpassResponse response = outpassService.getOutpass(studentUserId, role, outpassId);

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// GET ALL OUTPASSES
	// ============================================================

	@GetMapping("/outpasses/all")
	public ResponseEntity<List<OutpassResponse>> getOutpasses(Authentication authentication) {

		String studentUserId = getUserId(authentication);

		String role = getRole(authentication);

		List<OutpassResponse> response = outpassService.getOutpasses(studentUserId, role);

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// RESEND PARENT EMAIL
	// ============================================================

	@PostMapping("/outpasses/{outpassId}/resend-email")
	public ResponseEntity<ResendEmailResponse> resendEmail(@PathVariable Long outpassId,
			@Valid @RequestBody ResendEmailRequest request, Authentication authentication) {

		ResendEmailResponse response = outpassService.resendEmail(outpassId, request.getRecipient(), authentication);

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// CANCEL OUTPASS
	// ============================================================

	@PutMapping("/cancel/{id}")
	public ResponseEntity<String> cancelRequest(@PathVariable Long id, Authentication authentication) {

		String studentUserId = getUserId(authentication);

		outpassService.cancelRequest(id, studentUserId);

		return ResponseEntity.ok("Outpass request cancelled successfully");
	}

	// ============================================================
	// GET QR
	// ============================================================

	@GetMapping("/qr/{id}")
	public ResponseEntity<QrResponse> getQr(@PathVariable Long id, Authentication authentication) {

		String studentUserId = getUserId(authentication);

		QrResponse response = outpassService.getQr(id, studentUserId);

		return ResponseEntity.ok(response);
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