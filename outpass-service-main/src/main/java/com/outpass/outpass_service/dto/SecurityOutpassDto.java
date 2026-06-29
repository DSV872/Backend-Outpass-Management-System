package com.outpass.outpass_service.dto;

import java.time.LocalDateTime;

import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.model.OutpassType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityOutpassDto {
	
	
	private long id;
	private String studentEmail;
	private OutpassType outpassType;
	private LocalDateTime outTime;
	private LocalDateTime expectedInTime;
	private LocalDateTime actualIntime;
	private LocalDateTime actualOutTime;
	private OutpassStatus outpassStatus;
	
	

}
