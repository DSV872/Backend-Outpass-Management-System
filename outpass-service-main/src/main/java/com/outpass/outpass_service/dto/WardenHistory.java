package com.outpass.outpass_service.dto;

import java.time.LocalDateTime;

import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.model.OutpassType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WardenHistory {
	 private long id;
	 private String studentEmail;
	 private OutpassType type;
	 private LocalDateTime outTime;
	 private LocalDateTime inTime;
	 private OutpassStatus status;
}
