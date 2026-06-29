package com.outpass.outpass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class OutpassEvent {


    private Long outpassId;
    private String studentEmail;
    private String parentEmail;
    private String status;
    private String approvalToken;
    // getters & setters
	public OutpassEvent(Long outpassId, String studentEmail, String status) {
		super();
		this.outpassId = outpassId;
		this.studentEmail = studentEmail;
		this.status = status;
	}
    
    
    
    
 
    
}