package com.outpass.outpass_service.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;

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
public class ParentApprovalService {

	private final OutpassRepository outpassRepo;
	private final OutpassApprovalRepository approvalRepo;
	private final TokenService tokenService;
	private final KafkaTemplate<String, OutpassEvent> kafkaTemplate;
	private final ProfileServiceClient profileServiceClient;
	private final AuthServiceClient authServiceClient;
	@Transactional
	public void approve(String rawToken) {
		log.info("Parent approval request received");
		OutpassApproval parentApproval = validateToken(rawToken);
		Outpass outpass = parentApproval.getOutpass();
		List<WardenDutyResponse> duties;
		try {
			duties = profileServiceClient.getTodaysWardenDuties();
		} catch (FeignException.NotFound e) {
			log.warn("No warden assigned for today: outpassId={}", outpass.getId());
			throw new WardenDutyException("No warden is assigned for today");
		} catch (FeignException e) {
			log.error("Failed to retrieve today's warden duties: outpassId={}, status={}", outpass.getId(), e.status(),
					e);
			throw new WardenDutyException("Unable to retrieve today's warden duties", e);
		}
		if (duties == null || duties.isEmpty()) {
			log.warn("No warden assigned for today: outpassId={}", outpass.getId());
			throw new WardenDutyException("No warden is assigned for today");
		}
		List<WardenDutyResponse> onDutyWardens = duties.stream().filter(duty -> duty.getWardenUserId() != null)
				.filter(duty -> !duty.getWardenUserId().isBlank())
				.filter(duty -> duty.getStatus() == DutyStatus.ON_DUTY).toList();
		if (onDutyWardens.isEmpty()) {
			log.warn("No warden currently on duty: outpassId={}", outpass.getId());
			throw new WardenDutyException("No warden is on duty today");
		}
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
		long assignedCount = approvalRepo.countByApproverTypeAndCreatedAtBetween(ApproverType.WARDEN, startOfDay,
				endOfDay);
		int wardenIndex = (int) (assignedCount % onDutyWardens.size());
		WardenDutyResponse selectedDuty = onDutyWardens.get(wardenIndex);
		String wardenUserId = selectedDuty.getWardenUserId();
		log.info("Warden selected using round-robin: outpassId={}, wardenUserId={}, index={}", outpass.getId(),
				wardenUserId, wardenIndex);
		parentApproval.setStatus(ApprovalStatus.APPROVED);
		parentApproval.setActionedAt(LocalDateTime.now());
		clearToken(parentApproval);
		OutpassApproval wardenApproval = new OutpassApproval();
		wardenApproval.setOutpass(outpass);
		wardenApproval.setApproverType(ApproverType.WARDEN);
		wardenApproval.setApproverUserId(wardenUserId);
		wardenApproval.setStatus(ApprovalStatus.PENDING);
		outpass.addApproval(wardenApproval);
		outpass.setStatus(OutpassStatus.PARENT_APPROVED);
		approvalRepo.save(parentApproval);
		approvalRepo.save(wardenApproval);
		outpassRepo.save(outpass);
		log.info("Parent approved outpass and warden assigned: outpassId={}, studentUserId={}, wardenUserId={}",
				outpass.getId(), outpass.getStudentUserId(), wardenUserId);
		UserValidationResponse student = authServiceClient.getUser(outpass.getStudentUserId());
		OutpassEvent event = new OutpassEvent(outpass.getId(), student.getEmail(),
				parentApproval.getApproverEmail(), wardenUserId, OutpassStatus.PARENT_APPROVED.name(), null);
		kafkaTemplate.send("outpass-events", event);
		log.info("Parent approval event published: outpassId={}, wardenUserId={}", outpass.getId(), wardenUserId);
	}

	@Transactional
	public void reject(String rawToken) {
		log.info("Parent rejection request received");
		OutpassApproval parentApproval = validateToken(rawToken);
		Outpass outpass = parentApproval.getOutpass();
		parentApproval.setStatus(ApprovalStatus.REJECTED);
		parentApproval.setActionedAt(LocalDateTime.now());
		clearToken(parentApproval);
		outpass.setStatus(OutpassStatus.REJECTED);
		approvalRepo.save(parentApproval);
		outpassRepo.save(outpass);
		log.info("Outpass rejected by parent: outpassId={}, studentUserId={}", outpass.getId(),
				outpass.getStudentUserId());
		UserValidationResponse student = authServiceClient.getUser(outpass.getStudentUserId());
		OutpassEvent event = new OutpassEvent(outpass.getId(), student.getEmail(),
				parentApproval.getApproverEmail(), null, OutpassStatus.REJECTED.name(), null);
		kafkaTemplate.send("outpass-events", event);
		log.info("Parent rejection event published: outpassId={}", outpass.getId());
	}

	private OutpassApproval validateToken(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			log.warn("Parent approval attempted with empty token");
			throw new OutpassBusinessException("Approval token cannot be empty");
		}
		String decodedToken = URLDecoder.decode(rawToken, StandardCharsets.UTF_8);
		String tokenHash = tokenService.hash(decodedToken);
		OutpassApproval parentApproval = approvalRepo.findByApprovalTokenHash(tokenHash).orElseThrow(() -> {
			log.warn("Invalid or expired parent approval link");
			return new OutpassBusinessException("Invalid or expired approval link");
		});
		if (parentApproval.getApproverType() != ApproverType.PARENT) {
			log.warn("Invalid parent approval link: outpassId={}", parentApproval.getOutpass().getId());
			throw new OutpassBusinessException("Invalid parent approval link");
		}
		if (parentApproval.getApprovalTokenExpiry() == null
				|| parentApproval.getApprovalTokenExpiry().isBefore(LocalDateTime.now())) {
			log.warn("Parent approval link expired: outpassId={}", parentApproval.getOutpass().getId());
			throw new OutpassBusinessException("Approval link expired");
		}
		if (parentApproval.getStatus() != ApprovalStatus.PENDING) {
			log.warn("Parent approval already processed: outpassId={}, status={}", parentApproval.getOutpass().getId(),
					parentApproval.getStatus());
			throw new OutpassBusinessException("Outpass parent approval has already been processed");
		}
		Outpass outpass = parentApproval.getOutpass();
		if (outpass.getStatus() != OutpassStatus.PENDING) {
			log.warn("Outpass no longer awaiting parent approval: outpassId={}, status={}", outpass.getId(),
					outpass.getStatus());
			throw new OutpassBusinessException("Outpass is no longer awaiting parent approval");
		}
		return parentApproval;
	}

	private void clearToken(OutpassApproval parentApproval) {
		parentApproval.setApprovalTokenHash(null);
		parentApproval.setApprovalTokenExpiry(null);
	}
}