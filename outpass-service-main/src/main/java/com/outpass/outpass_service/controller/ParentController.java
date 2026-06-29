package com.outpass.outpass_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.outpass_service.service.ParentApprovalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parent")
public class ParentController {
	private final ParentApprovalService parentApprovalService;
	
	@GetMapping("/approve")
	public ResponseEntity<String> approve(@RequestParam String token){
		parentApprovalService.approve(token);
		return ResponseEntity.ok("Outpass approved successfully");
	}
	
	@GetMapping("/reject")
	public ResponseEntity<String> reject(@RequestParam String token){
		parentApprovalService.reject(token);
		return ResponseEntity.ok("Outpass rejected");
	}
}
