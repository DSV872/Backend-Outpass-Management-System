package com.outpass.profile_service.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.outpass.profile_service.dto.WardenDutyRequest;
import com.outpass.profile_service.dto.WardenDutyResponse;
import com.outpass.profile_service.service.WardenDutyService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/profiles/warden-duties")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class WardenDutyController {

	private final WardenDutyService wardenDutyService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WardenDutyResponse> assignDuty(@Valid @RequestBody WardenDutyRequest request) {

		log.info("Admin assigning warden duty: userId={}, dutyDate={}", request.getWardenUserId(),
				request.getDutyDate());

		return ResponseEntity.status(HttpStatus.CREATED).body(wardenDutyService.assignDuty(request));
	}

	@GetMapping("/today")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<WardenDutyResponse>> getTodaysDuties() {

		return ResponseEntity.ok(
				wardenDutyService.getTodaysDuties()
		);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<WardenDutyResponse>> getDutiesByDate(
			@RequestParam LocalDate date) {

		return ResponseEntity.ok(
				wardenDutyService.getDutiesByDate(date)
		);
	}

	@PutMapping("/warden-duties/{dutyId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WardenDutyResponse> updateDuty(@PathVariable Long dutyId,
			@Valid @RequestBody WardenDutyRequest request) {

		return ResponseEntity.ok(wardenDutyService.updateDuty(dutyId, request));
	}
	
	@PatchMapping("/{dutyId}/replace")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<WardenDutyResponse> replaceDuty(
	        @PathVariable Long dutyId,
	        @RequestBody WardenDutyRequest request) {

	    return ResponseEntity.ok(
	            wardenDutyService.replaceDuty(dutyId, request)
	    );
	}
}