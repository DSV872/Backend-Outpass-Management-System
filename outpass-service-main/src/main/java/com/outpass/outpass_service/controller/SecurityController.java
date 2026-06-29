package com.outpass.outpass_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.outpass_service.dto.SecurityOutpassDto;
import com.outpass.outpass_service.model.Outpass;
import com.outpass.outpass_service.service.SecuirtyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/security")
public class SecurityController {
	
	private final SecuirtyService securityService;
	@PutMapping("/scan-out/{qrToken}")
	public ResponseEntity<String> scanOut(@RequestHeader("X-User-Email") String email,
			@RequestHeader("X-User-Role") String role,					
			@PathVariable String qrToken){
		securityService.approve(email,role,qrToken);
		return ResponseEntity.ok("Hooray! You can cross the gate");
	}
	
	@PutMapping("/scan-in/{qrToken}")
	public ResponseEntity<String> scanIn(
			@RequestHeader("X-User-Email") String email,
			@RequestHeader("X-User-Role") String role,
			@PathVariable String qrToken){
		securityService.scanIn(email,role,qrToken);
		return ResponseEntity.ok("Hooray! You can in the college");
	}
	
	@GetMapping("/history")
	public ResponseEntity<List<SecurityOutpassDto>> getSecurityHistory(){
		
		List<SecurityOutpassDto> outpasses=securityService.getSecurityHistory();
		
		return ResponseEntity.ok(outpasses);
		
	}
	
}
