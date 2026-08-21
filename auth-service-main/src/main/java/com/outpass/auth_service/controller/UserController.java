package com.outpass.auth_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.auth_service.dto.LoginDto;
import com.outpass.auth_service.dto.LoginResponseDto;
import com.outpass.auth_service.dto.RegisterDto;
import com.outpass.auth_service.dto.UpdateUserRequest;
import com.outpass.auth_service.dto.UserResponse;
import com.outpass.auth_service.dto.UserStatusRequest;
import com.outpass.auth_service.service.UserService;

//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	@PreAuthorize("hasRole('ADMIN')")
//	@Operation(security = { @SecurityRequirement(name = "bearerAuth") })
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterDto register) {
		return ResponseEntity.ok(userService.createUser(register));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto login) {
		return ResponseEntity.ok(userService.login(login));
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
//	@Operation(security = { @SecurityRequirement(name = "bearerAuth") })
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@PatchMapping("/users/{userId}/status")
	@PreAuthorize("hasRole('ADMIN')")
//	@Operation(security = { @SecurityRequirement(name = "bearerAuth") })
	public ResponseEntity<UserResponse> updateUserStatus(@PathVariable String userId,
			@Valid @RequestBody UserStatusRequest request) {
		return ResponseEntity.ok(userService.updateUserStatus(userId, request.getEnabled()));
	}

	@PutMapping("/users/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
//	@Operation(security = { @SecurityRequirement(name = "bearerAuth") })
	public ResponseEntity<UserResponse> updateUser(@PathVariable String userId,
			@Valid @RequestBody UpdateUserRequest request) {
		return ResponseEntity.ok(userService.updateUser(userId, request));
	}
}