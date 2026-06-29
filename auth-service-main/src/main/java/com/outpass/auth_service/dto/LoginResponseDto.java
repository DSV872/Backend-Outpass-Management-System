package com.outpass.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

	private String token;
	private String role;
	private long expiresIn;
}
