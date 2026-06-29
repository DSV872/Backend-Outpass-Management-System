package com.outpass.outpass_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.outpass.outpass_service.dto.OutpassEvent;
import com.outpass.outpass_service.dto.OutpassPendingResponse;
import com.outpass.outpass_service.dto.QrResponse;
import com.outpass.outpass_service.dto.WardenHistory;
import com.outpass.outpass_service.model.Outpass;
import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.repo.OutpassRepository;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardenService {
	
	private final OutpassRepository outpassRepo;
	private final QrTokenService qrTokenService;
	private final KafkaTemplate<String, OutpassEvent> kafkaTemplate;
	
	@Transactional
	public QrResponse approve(String email, String role, long outpassId) {
		if(!role.equals("WARDEN")) {
			throw new ForbiddenException("You have no access to this");
		}
		Outpass outpass = outpassRepo.findById(outpassId)
				.orElseThrow(()-> new RuntimeException("outpass is not present"));
		if(outpass.getStatus() == OutpassStatus.REJECTED) {
			throw new RuntimeException("Outpass is already rejected");
		}
		if (outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
		    throw new IllegalStateException(
		        "Only parent-approved outpasses can be processed by warden"
		    );
		}
		String qrToken = qrTokenService.generate(outpass.getId(),outpass.getStudentEmail(),outpass.getExpectedInTime());
		
		outpass.setStatus(OutpassStatus.WARDEN_APPROVED);
		outpass.setWardenEmail(email);
		outpass.setUpdatedAt(LocalDateTime.now());
		outpass.setWardenRemarks("Outpass is granted after parent approval");
		outpass.setWardenApprovedAt(LocalDateTime.now());
		outpass.setQrToken(qrToken);
		outpass.setQrGeneratedAt(LocalDateTime.now());
		outpassRepo.save(outpass);
		
		OutpassEvent event = new OutpassEvent(outpass.getId(), outpass.getStudentEmail(), outpass.getParentEmail(), "WARDEN_APPROVED", null);
		System.out.println("sending event with details "+event);
		kafkaTemplate.send("outpass-events",event);
		return new QrResponse(outpass.getId(), qrToken, outpass.getExpectedInTime());
	}
	
	@Transactional
	public String reject(String email, String role, long outpassId) {
		if(!role.equals("WARDEN")) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You have no access to this");
		}
		Outpass outpass = outpassRepo.findById(outpassId)
				.orElseThrow(()-> new RuntimeException("Outpass is not present"));
		if(outpass.getStatus() == OutpassStatus.REJECTED) {
			throw new RuntimeException("Outpass is already rejected");
		}
		if (outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
		    throw new IllegalStateException(
		        "Only parent-approved outpasses can be processed by warden"
		    );
		}

		outpass.setStatus(OutpassStatus.REJECTED);
		outpass.setWardenEmail(email);
		outpass.setWardenApprovedAt(LocalDateTime.now());
		outpass.setWardenRemarks("Outpass is rejected for some reasons");
		outpass.setUpdatedAt(LocalDateTime.now());
		
		outpassRepo.save(outpass);
		OutpassEvent event = new OutpassEvent(outpass.getId(), outpass.getStudentEmail(), outpass.getParentEmail(), "REJECTED", null);
		kafkaTemplate.send("outpass-events",event);
		return outpass.getOutpassType()+" is rejected";
	}

	public List<OutpassPendingResponse> getPendingOutpassList(String role) {
		if(!role.equals("WARDEN")) {
			throw new ForbiddenException("Access denied");
		}
		LocalDate today = LocalDate.now();

		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

		List<Outpass> outpassList =
		        outpassRepo.findAllByStatusAndOutTimeBetween(
		                OutpassStatus.PARENT_APPROVED,
		                startOfDay,
		                endOfDay
		        );		if (outpassList.isEmpty()) {
		}
		List<OutpassPendingResponse> responseList = new ArrayList<>();
		for(Outpass outpass : outpassList) {
			OutpassPendingResponse response = new OutpassPendingResponse();
			response.setId(outpass.getId());
			response.setOutpassType(outpass.getOutpassType());
			response.setDestination(outpass.getDestination());
			response.setOutTime(outpass.getOutTime());
			response.setInTime(outpass.getActualInTime());
			response.setStatus(outpass.getStatus());
			responseList.add(response);
		}
		return responseList;
		
	}

	public List<WardenHistory> getWardenApproveList(String email, String role) {
		if(!role.equals("WARDEN")) {
			throw new ForbiddenException("Access denied");
		}
		List<Outpass> outpassList = outpassRepo.findAllByWardenEmailAndStatusIn(email,List.of(OutpassStatus.WARDEN_APPROVED, OutpassStatus.REJECTED,OutpassStatus.OUT));

		List<WardenHistory> wardenHistory = new ArrayList<>();
		
		for(Outpass outpass : outpassList) {
			WardenHistory warden = new WardenHistory();
			warden.setId(outpass.getId());
			warden.setType(outpass.getOutpassType());
			warden.setStudentEmail(outpass.getStudentEmail());
			warden.setOutTime(outpass.getOutTime());
			warden.setInTime(outpass.getActualInTime());
			warden.setStatus(outpass.getStatus());
			wardenHistory.add(warden);
			
		}
		
		return wardenHistory;
	}

}
