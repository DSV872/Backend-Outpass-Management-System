package com.outpass.auth_service.dto;

import com.outpass.auth_service.model.RoleType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {

	@NotNull(message = "Role is required")
	private RoleType role;
}