package com.outpass.outpass_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseDto {
	private int status;
	private String error;
	private String message;
	private String path;
	private LocalDateTime timestamp;
}
