package com.outpass.outpass_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class QrPayload {
	
	private long outpassId;
	private String email;
}
