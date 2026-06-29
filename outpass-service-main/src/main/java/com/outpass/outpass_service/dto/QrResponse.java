package com.outpass.outpass_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class QrResponse {
	private long outpassId;
	private String qrToken;
	private LocalDateTime expiresAt;
}
