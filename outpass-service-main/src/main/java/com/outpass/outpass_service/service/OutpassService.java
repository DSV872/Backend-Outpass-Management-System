package com.outpass.outpass_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.outpass.outpass_service.dto.OutpassEvent;
import com.outpass.outpass_service.dto.OutpassRequest;
import com.outpass.outpass_service.dto.OutpassResponse;
import com.outpass.outpass_service.dto.QrResponse;
import com.outpass.outpass_service.model.Outpass;
import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.model.OutpassType;
import com.outpass.outpass_service.repo.OutpassRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutpassService {

	private final OutpassRepository outpassRepo;
	private final TokenService tokenService;
	private final KafkaTemplate<String, OutpassEvent> kafkaTemplate;

	@Transactional
	public String apply(@Valid OutpassRequest outpassRequest, String studentEmail, String role) {
		
		if (!role.equals("STUDENT")) {
			throw new IllegalStateException("Only students can apply for outpass");
		}

		boolean hasActiveOutpass = outpassRepo.existsByStudentEmailAndStatusIn(studentEmail,
				List.of(OutpassStatus.PENDING, OutpassStatus.PARENT_APPROVED, OutpassStatus.WARDEN_APPROVED,OutpassStatus.OUT));

		if (hasActiveOutpass) {
			throw new IllegalStateException("You already have an active "+outpassRequest.getOutpassType());
		}
		if (outpassRequest.getOutTime().isBefore(LocalDateTime.now())) {
			throw new IllegalStateException(outpassRequest.getOutpassType() + " cannot be applied for the past time");
		}
		if (!outpassRequest.getExpectedInTime().isAfter(outpassRequest.getOutTime())) {
			throw new IllegalStateException("Expected-in-time must be after out-time");
		}
		if (outpassRequest.getOutpassType() == OutpassType.OUTING) {
			if (!outpassRequest.getOutTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault()))) {
				throw new IllegalStateException(outpassRequest.getOutpassType() + " can be applied for today only");
			}
			if (!outpassRequest.getExpectedInTime().toLocalDate().equals(outpassRequest.getOutTime().toLocalDate())) {

				throw new IllegalStateException("OUTING must start and end on the same day");
			}
		}
		if (outpassRequest.getOutpassType() == OutpassType.OUTPASS) {
			if (!outpassRequest.getExpectedInTime().toLocalDate().isAfter(outpassRequest.getOutTime().toLocalDate())) {
				throw new IllegalStateException(outpassRequest.getOutpassType() + " expected-in date must be after the out date");
			}
		}
		
		String rawToken = tokenService.generateToken();
		String tokenHash = tokenService.hash(rawToken);

		Outpass outpass = toEntity(outpassRequest, studentEmail, tokenHash);
		outpassRepo.save(outpass);

		OutpassEvent event = new OutpassEvent(outpass.getId(), outpass.getStudentEmail(), outpass.getParentEmail(),
				"PENDING", rawToken);

		kafkaTemplate.send("outpass-events", event);

		return rawToken;
	}

	public void approveOutpass(Long id) {

		Outpass outpass = outpassRepo.findById(id).orElseThrow(() -> new RuntimeException("Outpass not found"));

		outpass.setStatus(OutpassStatus.WARDEN_APPROVED);
		outpassRepo.save(outpass);

		// 🔥 Publish Kafka Event
		OutpassEvent event = new OutpassEvent(outpass.getId(), outpass.getStudentEmail(), "APPROVED");

		kafkaTemplate.send("outpass-events", event);
		System.out.println("Event sent to Kafka: " + event.getStatus());
	}

	public void rejectOutpass(Long id) {

		Outpass outpass = outpassRepo.findById(id).orElseThrow(() -> new RuntimeException("Outpass not found"));

		outpass.setStatus(OutpassStatus.REJECTED);
		outpassRepo.save(outpass);

		OutpassEvent event = new OutpassEvent(outpass.getId(), outpass.getStudentEmail(), "REJECTED");

		kafkaTemplate.send("outpass-events", event);
	}

	private Outpass toEntity(@Valid OutpassRequest request, String studentEmail, String tokenHash) {

		Outpass outpass = new Outpass();
		outpass.setStudentEmail(studentEmail);
		outpass.setOutpassType(request.getOutpassType());
		outpass.setReason(request.getReason());
		outpass.setDestination(request.getDestination());
		outpass.setParentEmail(request.getParentEmail());
		outpass.setOutTime(request.getOutTime());
		outpass.setExpectedInTime(request.getExpectedInTime());
		outpass.setCreatedAt(LocalDateTime.now());
		outpass.setStatus(OutpassStatus.PENDING);
		outpass.setParentApprovalTokenHash(tokenHash);
		outpass.setParentApprovalTokenExpiry(LocalDateTime.now().plusHours(12));

		return outpass;
	}

	public OutpassResponse getOutpassList(String studentEmail, String role, long outpassId) {

		// 1️⃣ Role validation
		if (!"STUDENT".equals(role)) {
			throw new IllegalStateException("Only students can access this outpass");
		}

		// 2️⃣ Fetch outpass
		Outpass outpass = outpassRepo.findById(outpassId).orElseThrow(() -> new RuntimeException("Outpass not found"));

		// 3️⃣ Ownership validation (VERY IMPORTANT)
		if (!outpass.getStudentEmail().equals(studentEmail)) {
			throw new IllegalStateException("You are not authorized to view this outpass");
		}

		// 4️⃣ Convert to DTO
		OutpassResponse response = new OutpassResponse();
		response.setId(outpass.getId());
		response.setDestination(outpass.getDestination());
		response.setOutTime(outpass.getOutTime());
		response.setInTime(outpass.getExpectedInTime());
		response.setOutpassType(outpass.getOutpassType());
		response.setStatus(outpass.getStatus());

		return response;
	}

	@Transactional
	public void cancelRequest(Long id, String email) throws Exception {
		// TODO Auto-generated method stub

		Outpass outpass = outpassRepo.findById(id).orElseThrow(() -> (new RuntimeException("Outpass Not Found")));

		if (!outpass.getStudentEmail().equals(email)) {
			throw new Exception("Unauthorized access");
		}

		if (outpass.getStatus() != OutpassStatus.PENDING && outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
			throw new Exception("Cannot cancel at this stage");

		}

		outpass.setStatus(OutpassStatus.CANCELLED);
		outpassRepo.save(outpass);

	}

	public List<OutpassResponse> getOutpasses(String email, String role) {
		if (!role.equals("STUDENT")) {
			throw new ForbiddenException("Access denied");
		}
		List<Outpass> outpassList = outpassRepo.findAllByStudentEmail(email);
		List<OutpassResponse> outpassListResponse = new ArrayList<>();
		for (Outpass outpass : outpassList) {
			OutpassResponse response = new OutpassResponse();
			response.setId(outpass.getId());
			response.setOutpassType(outpass.getOutpassType());
			response.setReason(outpass.getReason());
			response.setDestination(outpass.getDestination());
			response.setOutTime(outpass.getOutTime());
			response.setExpectedInTime(outpass.getExpectedInTime());
			response.setInTime(outpass.getActualInTime());
			response.setStatus(outpass.getStatus());
			outpassListResponse.add(response);
		}
		return outpassListResponse;
	}

	public QrResponse getQr(Long id, String email) {
		Outpass outpass = outpassRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Outpass not found"));
		if (!outpass.getStudentEmail().equals(email)) {
			throw new ForbiddenException("Access denied");
		}
		if (outpass.getStatus() != OutpassStatus.WARDEN_APPROVED && outpass.getStatus() != OutpassStatus.OUT) {
			throw new IllegalStateException("QR not available for this outpass");
		}
		return new QrResponse(outpass.getId(), outpass.getQrToken(), outpass.getExpectedInTime());
	}

	@Transactional
	public void cancelExpiredOutpasses() {
		LocalDateTime startOfToday =
		        LocalDate.now().atStartOfDay();
		List<Outpass> outpasses = outpassRepo.findExpiredOutpasses(startOfToday);
		for(Outpass outpass : outpasses) {
				outpass.setStatus(OutpassStatus.CANCELLED);
			}
		
		outpassRepo.saveAll(outpasses);
	}
}