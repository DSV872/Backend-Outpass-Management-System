package com.outpass.outpass_service.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.outpass.outpass_service.dto.OutpassEvent;
import com.outpass.outpass_service.model.Outpass;
import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.repo.OutpassRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParentApprovalService {
	
	private final OutpassRepository outpassRepo;
	private final TokenService tokenService;
    private final KafkaTemplate<String, OutpassEvent> kafkaTemplate;


    @Transactional
	public void approve(String rawToken) {
		Outpass outpass = validateToken(rawToken);
		
		outpass.setStatus(OutpassStatus.PARENT_APPROVED);
		outpass.setParentApprovedAt(LocalDateTime.now());
		outpass.setUpdatedAt(LocalDateTime.now());
		clearToken(outpass);
		outpassRepo.save(outpass);
		
		OutpassEvent event = new OutpassEvent(outpass.getId(),outpass.getStudentEmail(),outpass.getParentEmail(),"PARENT_APPROVED",null);
		kafkaTemplate.send("outpass-events",event);
	}

	private void clearToken(Outpass outpass) {
		outpass.setParentApprovalTokenHash(null);
		outpass.setParentApprovalTokenExpiry(null);
		
	}

	private Outpass validateToken(String rawToken) {
		String decodedToken = URLDecoder.decode(rawToken,StandardCharsets.UTF_8);
		String tokenHash = tokenService.hash(decodedToken);
		System.out.println(tokenHash);
		
		Outpass outpass = outpassRepo.findByParentApprovalTokenHash(tokenHash)
				.orElseThrow(()->new IllegalStateException("Invalid or expired approval link"));
		
		 if(outpass.getParentApprovalTokenExpiry().isBefore(LocalDateTime.now())) {
			 throw new IllegalStateException("Approval link expired");
		 }
		 
		 if(outpass.getStatus() != OutpassStatus.PENDING) {
			 throw new IllegalStateException("Outpass already processed");
		 }
		 return outpass;
	}

	@Transactional
	public void reject(String token) {
		Outpass outpass = validateToken(token);
		outpass.setStatus(OutpassStatus.REJECTED);
		outpass.setParentApprovedAt(LocalDateTime.now());
		outpass.setUpdatedAt(LocalDateTime.now());
		
		clearToken(outpass);
		outpassRepo.save(outpass);
		
		OutpassEvent event = new OutpassEvent(outpass.getId(), outpass.getStudentEmail(), outpass.getParentEmail(), "REJECTED", null);
		kafkaTemplate.send("outpass-events",event);
	}

}
