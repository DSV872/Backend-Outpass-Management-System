package com.outpass.outpass_service.dto;

import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.model.OutpassType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityOutpassDto {

	private Long id;

	private String studentUserId;

	private OutpassType outpassType;

	private LocalDateTime outTime;

	private LocalDateTime expectedInTime;

	private LocalDateTime actualOutTime;

	private LocalDateTime actualIntime;

	private OutpassStatus outpassStatus;
}