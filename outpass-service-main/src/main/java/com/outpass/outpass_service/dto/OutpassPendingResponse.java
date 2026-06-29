package com.outpass.outpass_service.dto;

import java.time.LocalDateTime;

import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.model.OutpassType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutpassPendingResponse {
	private long id;
	private OutpassType outpassType;
	private OutpassStatus status;
	private LocalDateTime outTime;
	private LocalDateTime inTime;
	private String destination;
}
