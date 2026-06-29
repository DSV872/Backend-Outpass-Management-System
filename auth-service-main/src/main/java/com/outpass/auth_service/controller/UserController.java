package com.outpass.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.auth_service.dto.LoginDto;
import com.outpass.auth_service.dto.LoginResponseDto;
import com.outpass.auth_service.dto.RegisterDto;
import com.outpass.auth_service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

	private final UserService userService; 
	
	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterDto register){
		userService.register(register);
		return ResponseEntity.ok("Registration success! ");
	}
	
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto login){
		return ResponseEntity.ok(userService.login(login));
		
	}
}
