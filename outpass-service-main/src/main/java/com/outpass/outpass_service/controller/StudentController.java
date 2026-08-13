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

	@PostMapping("/apply")
	public ResponseEntity<String> apply(@Valid @RequestBody OutpassRequest outpassRequest,
			Authentication authentication) {

		String studentEmail = authentication.getName();

		String role = getRole(authentication);

		String token = outpassService.apply(outpassRequest, studentEmail, role);

		return ResponseEntity.ok("outpass is applied successfully! " + token);
	}

	@GetMapping("/outpasses/{outpassId}")
	public ResponseEntity<OutpassResponse> outpassList(Authentication authentication,
			@PathVariable("outpassId") long outpassId) {

		String studentEmail = authentication.getName();

		String role = getRole(authentication);

		return ResponseEntity.ok(outpassService.getOutpassList(studentEmail, role, outpassId));
	}

	@GetMapping("/outpasses/all")
	public ResponseEntity<List<OutpassResponse>> outpassList(Authentication authentication) {

		String email = authentication.getName();

		String role = getRole(authentication);

		List<OutpassResponse> response = outpassService.getOutpasses(email, role);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/approve")
	public ResponseEntity<String> approveOutpass(@PathVariable Long id) {

		outpassService.approveOutpass(id);

		return ResponseEntity.ok("Outpass approved");
	}
	
	@PostMapping("/outpasses/{outpassId}/resend-email")
	public ResponseEntity<ResendEmailResponse> resendEmail(
	        @PathVariable Long outpassId,
	        @Valid @RequestBody ResendEmailRequest request,
	        Authentication authentication) {

	    ResendEmailResponse response =
	            outpassService.resendEmail(
	                    outpassId,
	                    request.getRecipient(),
	                    authentication
	            );

	    return ResponseEntity.ok(response);
	}

	@PutMapping("/cancel/{id}")
	public ResponseEntity<String> cancelRequest(@PathVariable Long id, Authentication authentication) throws Exception {

		String email = authentication.getName();

		outpassService.cancelRequest(id, email);

		return ResponseEntity.ok("Outpass Request Cancelled successfullly!");
	}

	@GetMapping("/qr/{id}")
	public ResponseEntity<QrResponse> getQr(@PathVariable Long id, Authentication authentication) {

		String email = authentication.getName();

		QrResponse response = outpassService.getQr(id, email);

		return ResponseEntity.ok(response);
	}

	private String getRole(Authentication authentication) {

		return authentication.getAuthorities().stream().findFirst().map(authority -> authority.getAuthority())
				.map(authority -> authority.replace("ROLE_", "")).orElseThrow();
	}
}