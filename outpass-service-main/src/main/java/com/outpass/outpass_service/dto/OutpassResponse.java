package com.outpass.outpass_service.dto;

import java.time.LocalDateTime;

import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.model.OutpassType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutpassResponse {
		private long id;
		private String reason;
		private OutpassType outpassType;
		private String Destination;
		private OutpassStatus status;
		private LocalDateTime outTime;
		private LocalDateTime expectedInTime;
		private LocalDateTime inTime;
}
