package com.outpass.outpass_service.service;

import java.time.*;
import java.util.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.outpass.outpass_service.client.AuthServiceClient;
import com.outpass.outpass_service.client.ProfileServiceClient;
import com.outpass.outpass_service.dto.*;
import com.outpass.outpass_service.enums.DutyStatus;
import com.outpass.outpass_service.exception.*;
import com.outpass.outpass_service.model.*;
import com.outpass.outpass_service.repo.*;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WardenService {
	private final OutpassRepository outpassRepo;
	private final OutpassApprovalRepository approvalRepo;
	private final OutpassQrRepository qrRepo;
	private final ProfileServiceClient profileServiceClient;
	private final QrTokenService qrTokenService;
	private final KafkaTemplate<String, OutpassEvent> kafkaTemplate;
	private final AuthServiceClient authServiceClient;
	@Transactional
	public QrResponse approve(String wardenUserId, String role, Long outpassId) {
		log.info("Warden approval request: outpassId={}, wardenUserId={}", outpassId, wardenUserId);
		validateWardenRole(role);
		Outpass outpass = outpassRepo.findById(outpassId).orElseThrow(() -> {
			log.warn("Outpass not found: outpassId={}", outpassId);
			return new OutpassNotFoundException("Outpass is not present: " + outpassId);
		});
		if (outpass.getStatus() == OutpassStatus.REJECTED) {
			throw new OutpassBusinessException("Outpass is already rejected");
		}
		if (outpass.getStatus() == OutpassStatus.WARDEN_APPROVED) {
			throw new OutpassBusinessException("Warden has already approved this outpass");
		}
		if (outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
			throw new OutpassBusinessException("Only parent-approved outpasses can be processed by warden");
		}
		OutpassApproval wardenApproval = approvalRepo.findByOutpassIdAndApproverType(outpassId, ApproverType.WARDEN)
				.orElseThrow(() -> {
					log.warn("Warden approval assignment not found: outpassId={}", outpassId);
					return new OutpassNotFoundException("Warden approval assignment not found");
				});
		if (wardenApproval.getApproverUserId() == null || wardenApproval.getApproverUserId().isBlank()) {
			throw new WardenDutyException("No warden is assigned to this outpass");
		}
		if (!wardenUserId.equals(wardenApproval.getApproverUserId())) {
			log.warn("Unauthorized warden approval attempt: outpassId={}, wardenUserId={}", outpassId, wardenUserId);
			throw new UnauthorizedOutpassException("You are not assigned to approve this outpass");
		}
		if (wardenApproval.getStatus() != ApprovalStatus.PENDING) {
			throw new OutpassBusinessException("Warden has already processed this outpass");
		}
		OutpassQr existingQr = qrRepo.findByOutpassId(outpassId).orElse(null);
		if (existingQr != null) {
			throw new OutpassBusinessException("QR already exists for this outpass");
		}
		wardenApproval.setStatus(ApprovalStatus.APPROVED);
		wardenApproval.setActionedAt(LocalDateTime.now());
		wardenApproval.setRemarks("Outpass is granted after parent approval");
		approvalRepo.save(wardenApproval);
		String qrToken = qrTokenService.generate(outpass.getId(), outpass.getStudentUserId(),
				outpass.getExpectedInTime());
		OutpassQr qr = new OutpassQr();
		qr.setOutpass(outpass);
		qr.setQrToken(qrToken);
		qr.setGeneratedAt(LocalDateTime.now());
		qr.setExpiresAt(outpass.getExpectedInTime());
		qrRepo.save(qr);
		outpass.setStatus(OutpassStatus.WARDEN_APPROVED);
		outpassRepo.save(outpass);
		
		UserValidationResponse student = authServiceClient.getUser(outpass.getStudentUserId());
		OutpassEvent event = new OutpassEvent(outpass.getId(), student.getEmail(), null,
				wardenApproval.getApproverUserId(), OutpassStatus.WARDEN_APPROVED.name(), null);
		kafkaTemplate.send("outpass-events", event);
		log.info("Outpass approved by warden: outpassId={}, wardenUserId={}", outpassId, wardenUserId);
		return new QrResponse(outpass.getId(), qrToken, qr.getExpiresAt());
	}

	@Transactional
	public String reject(String wardenUserId, String role, Long outpassId) {
		log.info("Warden rejection request: outpassId={}, wardenUserId={}", outpassId, wardenUserId);
		validateWardenRole(role);
		Outpass outpass = outpassRepo.findById(outpassId).orElseThrow(() -> {
			log.warn("Outpass not found: outpassId={}", outpassId);
			return new OutpassNotFoundException("Outpass is not present: " + outpassId);
		});
		if (outpass.getStatus() == OutpassStatus.REJECTED) {
			throw new OutpassBusinessException("Outpass is already rejected");
		}
		if (outpass.getStatus() == OutpassStatus.WARDEN_APPROVED) {
			throw new OutpassBusinessException("Outpass has already been approved by warden");
		}
		if (outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
			throw new OutpassBusinessException("Only parent-approved outpasses can be processed by warden");
		}
		OutpassApproval wardenApproval = approvalRepo.findByOutpassIdAndApproverType(outpassId, ApproverType.WARDEN)
				.orElseThrow(() -> {
					log.warn("Warden approval assignment not found: outpassId={}", outpassId);
					return new OutpassNotFoundException("Warden approval assignment not found");
				});
		if (wardenApproval.getApproverUserId() == null || wardenApproval.getApproverUserId().isBlank()) {
			throw new WardenDutyException("No warden is assigned to this outpass");
		}
		if (!wardenUserId.equals(wardenApproval.getApproverUserId())) {
			log.warn("Unauthorized warden rejection attempt: outpassId={}, wardenUserId={}", outpassId, wardenUserId);
			throw new UnauthorizedOutpassException("You are not assigned to reject this outpass");
		}
		if (wardenApproval.getStatus() != ApprovalStatus.PENDING) {
			throw new OutpassBusinessException("Warden has already processed this outpass");
		}
		wardenApproval.setStatus(ApprovalStatus.REJECTED);
		wardenApproval.setActionedAt(LocalDateTime.now());
		wardenApproval.setRemarks("Outpass is rejected by warden");
		approvalRepo.save(wardenApproval);
		outpass.setStatus(OutpassStatus.REJECTED);
		outpassRepo.save(outpass);
		UserValidationResponse student = authServiceClient.getUser(outpass.getStudentUserId());
		OutpassEvent event = new OutpassEvent(outpass.getId(), student.getEmail(), null,
				wardenApproval.getApproverUserId(), OutpassStatus.REJECTED.name(), null);
		kafkaTemplate.send("outpass-events", event);
		log.info("Outpass rejected by warden: outpassId={}, wardenUserId={}", outpassId, wardenUserId);
		return outpass.getOutpassType() + " is rejected";
	}

	public List<OutpassPendingResponse> getPendingOutpassList(String wardenUserId, String role) {
		validateWardenRole(role);
		List<WardenDutyResponse> duties;
		try {
			duties = profileServiceClient.getTodaysWardenDuties();
		} catch (FeignException.NotFound e) {
			log.warn("No warden assigned for today");
			throw new WardenDutyException("No warden is assigned for today");
		} catch (FeignException e) {
			log.error("Failed to retrieve today's warden duties: status={}", e.status(), e);
			throw new WardenDutyException("Unable to retrieve today's warden duties", e);
		}
		if (duties == null || duties.isEmpty()) {
			throw new WardenDutyException("No warden is assigned for today");
		}
		boolean assignedToday = duties.stream()
				.anyMatch(duty -> duty.getWardenUserId() != null && !duty.getWardenUserId().isBlank()
						&& duty.getStatus() == DutyStatus.ON_DUTY && wardenUserId.equals(duty.getWardenUserId()));
		if (!assignedToday) {
			log.warn("User is not assigned as today's warden: wardenUserId={}", wardenUserId);
			throw new UnauthorizedOutpassException("You are not assigned as a warden for today");
		}
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
		List<Outpass> outpassList = outpassRepo
				.findByStatusAndOutTimeBetweenAndApprovals_ApproverTypeAndApprovals_ApproverUserIdAndApprovals_Status(
						OutpassStatus.PARENT_APPROVED, startOfDay, endOfDay, ApproverType.WARDEN, wardenUserId,
						ApprovalStatus.PENDING);
		List<OutpassPendingResponse> responseList = new ArrayList<>();
		for (Outpass outpass : outpassList) {
			OutpassPendingResponse response = new OutpassPendingResponse();
			response.setId(outpass.getId());
			response.setOutpassType(outpass.getOutpassType());
			response.setDestination(outpass.getDestination());
			response.setOutTime(outpass.getOutTime());
			response.setInTime(outpass.getExpectedInTime());
			response.setStatus(outpass.getStatus());
			responseList.add(response);
		}
		log.debug("Pending outpasses retrieved: wardenUserId={}, count={}", wardenUserId, responseList.size());
		return responseList;
	}

	public List<WardenHistory> getWardenApproveList(String wardenUserId, String role) {
		validateWardenRole(role);
		List<OutpassApproval> approvals = approvalRepo.findByApproverUserIdAndApproverType(wardenUserId,
				ApproverType.WARDEN);
		List<WardenHistory> history = new ArrayList<>();
		for (OutpassApproval approval : approvals) {
			Outpass outpass = approval.getOutpass();
			if (outpass.getStatus() != OutpassStatus.WARDEN_APPROVED && outpass.getStatus() != OutpassStatus.REJECTED
					&& outpass.getStatus() != OutpassStatus.OUT && outpass.getStatus() != OutpassStatus.IN) {
				continue;
			}
			WardenHistory warden = new WardenHistory();
			warden.setId(outpass.getId());
			warden.setType(outpass.getOutpassType());
			warden.setStudentUserId(outpass.getStudentUserId());
			warden.setOutTime(outpass.getOutTime());
			warden.setInTime(outpass.getActualInTime());
			warden.setStatus(outpass.getStatus());
			history.add(warden);
		}
		log.debug("Warden history retrieved: wardenUserId={}, count={}", wardenUserId, history.size());
		return history;
	}

	private void validateWardenRole(String role) {
		if (!"WARDEN".equalsIgnoreCase(role)) {
			log.warn("Unauthorized warden operation: role={}", role);
			throw new UnauthorizedOutpassException("Access denied. WARDEN role required");
		}
	}
}