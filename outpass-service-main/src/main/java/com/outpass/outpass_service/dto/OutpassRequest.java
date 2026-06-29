package com.outpass.outpass_service.dto;

import java.time.LocalDateTime;

import com.outpass.outpass_service.model.OutpassType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutpassRequest {
	
	@NotNull
	private OutpassType outpassType;
	
	@NotBlank
	private String reason;
	
	@NotBlank
	private String destination;
	
	@NotNull
	private LocalDateTime outTime;
	
	@NotNull
	private LocalDateTime expectedInTime;
	
	@NotBlank
	private String parentEmail;
	
	
}
