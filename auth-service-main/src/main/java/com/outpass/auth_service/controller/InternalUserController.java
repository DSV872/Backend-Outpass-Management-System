package com.outpass.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.outpass.auth_service.dto.UserValidationResponse;
import com.outpass.auth_service.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

	private final UserService authService;

	@GetMapping("/{userId}")
	public ResponseEntity<UserValidationResponse> getUser(@PathVariable String userId) {
		return ResponseEntity.ok(authService.validateUser(userId));
	}
}
