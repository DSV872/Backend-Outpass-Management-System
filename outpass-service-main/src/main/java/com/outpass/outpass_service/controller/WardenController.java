package com.outpass.outpass_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.outpass_service.dto.OutpassPendingResponse;
import com.outpass.outpass_service.dto.QrResponse;
import com.outpass.outpass_service.dto.WardenHistory;
import com.outpass.outpass_service.service.WardenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/warden")
@RequiredArgsConstructor
public class WardenController {
	private final WardenService wardenService;
	
	@PutMapping("/approve/{outpassId}")
	public ResponseEntity<QrResponse> approve(@RequestHeader("X-User-Email") String email, @RequestHeader("X-User-Role") String role,@PathVariable long outpassId){
		QrResponse qr = wardenService.approve(email,role,outpassId);
		return ResponseEntity.ok(qr);
	}
	
	@PutMapping("/reject/{outpassId}")
	public ResponseEntity<String> reject(@RequestHeader("X-User-Email") String email, @RequestHeader("X-User-Role") String role,@PathVariable long outpassId){
		wardenService.reject(email,role,outpassId);
		return ResponseEntity.ok("warden rejected sucessfully");
	}
	
	@GetMapping("/pending")
	public ResponseEntity<List<OutpassPendingResponse>> pendingOutpassList(@RequestHeader("X-User-Role") String role){
		List<OutpassPendingResponse> response = wardenService.getPendingOutpassList(role);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/history")
	public ResponseEntity<List<WardenHistory>> wardenHistory(@RequestHeader("X-User-Email") String email, @RequestHeader("X-User-Role") String role){
		List<WardenHistory> wardenApproveList = wardenService.getWardenApproveList(email,role);
		return ResponseEntity.ok(wardenApproveList);
	}
}
