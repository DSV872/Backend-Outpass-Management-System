package com.outpass.auth_service.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class RegisterDto {
	
	@NotBlank
	@Email
	private String email;
	
	@NotBlank
	@Size(min = 8)
	private String password;
}
