package com.outpass.outpass_service.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outpass.outpass_service.dto.OutpassRequest;
import com.outpass.outpass_service.dto.OutpassResponse;
import com.outpass.outpass_service.dto.QrResponse;
import com.outpass.outpass_service.service.OutpassService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
	
	private final OutpassService outpassService;
	
	@PostMapping("/apply")
	public ResponseEntity<String> apply(@Valid @RequestBody OutpassRequest outpassRequest,
										@RequestHeader("X-User-Email") String studentEmail,
										@RequestHeader("X-User-Role") String role){
		System.out.println("Email: " + studentEmail);
		System.out.println("Role: " + role);
		String token = outpassService.apply(outpassRequest,studentEmail, role);
		return ResponseEntity.ok("outpass is applied successfully!  " +token);
	}
	
	@GetMapping("/outpasses/{outpassId}")
	public ResponseEntity<OutpassResponse> outpassList(@RequestHeader("X-User-Email") String studentEmail,
													@RequestHeader("X-User-Role") String role, @PathVariable("outpassId") long outpassId){
		return ResponseEntity.ok(outpassService.getOutpassList(studentEmail,role,outpassId));
	}
	
	@GetMapping("/outpasses/all")
	public ResponseEntity<List<OutpassResponse>> outpassList(@RequestHeader("X-User-Email") String email, @RequestHeader("X-User-Role") String role){
		List<OutpassResponse> response = outpassService.getOutpasses(email,role);
		return ResponseEntity.ok(response);
	}
	
	
	@PutMapping("/{id}/approve")
	public ResponseEntity<String> approveOutpass(@PathVariable Long id) {
	    outpassService.approveOutpass(id);
	    return ResponseEntity.ok("Outpass approved");
	}
	
	@PutMapping("/cancel/{id}")
	public ResponseEntity<String> cancelRequest(@PathVariable Long id,@RequestHeader("X-User-Email") String email ) throws Exception{
		
		outpassService.cancelRequest(id,email);
		return ResponseEntity.ok("Outpass Request Cancelled successfullly!");
	}
	
	@GetMapping("/qr/{id}")
	public ResponseEntity<QrResponse> getQr(@PathVariable Long id, @RequestHeader("X-User-Email") String email){
		QrResponse response = outpassService.getQr(id,email);
		return ResponseEntity.ok(response);
	}
	
	
}
