package com.outpass.auth_service.dto;

import com.outpass.auth_service.model.RoleType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
	private String password;

	@NotNull(message = "Role is required")
	private RoleType role;
	
	@Email
	private String email;
}