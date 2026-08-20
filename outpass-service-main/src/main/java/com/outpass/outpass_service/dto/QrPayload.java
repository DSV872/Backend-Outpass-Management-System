package com.outpass.outpass_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class QrPayload {
	
	public QrPayload(Long outpassId2) {
		this.outpassId = outpassId2;
	}
	private Long outpassId;
	private String email;
}
