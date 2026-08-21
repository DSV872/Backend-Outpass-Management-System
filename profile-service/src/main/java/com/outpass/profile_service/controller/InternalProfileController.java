package com.outpass.profile_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.profile_service.dto.StudentParentResponse;
import com.outpass.profile_service.dto.StudentProfileResponse;
import com.outpass.profile_service.dto.WardenDutyResponse;
import com.outpass.profile_service.exception.WardenDutyNotFoundException;
import com.outpass.profile_service.service.StudentProfileService;
import com.outpass.profile_service.service.WardenDutyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalProfileController {

	private final StudentProfileService studentProfileService;
	private final WardenDutyService wardenDutyService;

	@GetMapping("/students/{userId}")
	public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable String userId) {

		return ResponseEntity.ok(studentProfileService.getInternalStudentProfile(userId));
	}

	@GetMapping("/students/{userId}/parent")
	public ResponseEntity<StudentParentResponse> getStudentParent(@PathVariable String userId) {

		return ResponseEntity.ok(studentProfileService.getStudentParent(userId));
	}

	@GetMapping("/warden-duties/today")
	public ResponseEntity<List<WardenDutyResponse>> getTodaysWardenDuty() {

		List<WardenDutyResponse> response = wardenDutyService.getTodaysDuties();
		return ResponseEntity.ok(response);
	}
}