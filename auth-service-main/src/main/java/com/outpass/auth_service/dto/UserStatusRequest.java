package com.outpass.auth_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRequest {

	@NotNull(message = "Enabled status is required")
	private Boolean enabled;
}