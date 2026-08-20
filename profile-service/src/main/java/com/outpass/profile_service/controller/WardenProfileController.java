package com.outpass.profile_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.outpass.profile_service.dto.WardenProfileCreateRequest;
import com.outpass.profile_service.dto.WardenProfileResponse;
import com.outpass.profile_service.dto.WardenProfileUpdateRequest;
import com.outpass.profile_service.service.WardenProfileService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profiles/wardens")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WardenProfileController {

	private final WardenProfileService wardenProfileService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WardenProfileResponse> create(@Valid @RequestBody WardenProfileCreateRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(wardenProfileService.create(request));
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
	public ResponseEntity<WardenProfileResponse> getByUserId(@PathVariable String userId) {

		return ResponseEntity.ok(wardenProfileService.getByUserId(userId));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<WardenProfileResponse>> getAll() {

		return ResponseEntity.ok(wardenProfileService.getAll());
	}

	@PutMapping("/{userId}")
	@PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
	public ResponseEntity<WardenProfileResponse> update(@PathVariable String userId,
			@Valid @RequestBody WardenProfileUpdateRequest request) {

		return ResponseEntity.ok(wardenProfileService.update(userId, request));
	}

	@DeleteMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable String userId) {

		wardenProfileService.delete(userId);

		return ResponseEntity.noContent().build();
	}
}