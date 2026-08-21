package com.outpass.outpass_service.service;

import java.time.*;
import java.util.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.outpass.outpass_service.exception.*;
import com.outpass.outpass_service.client.AuthServiceClient;
import com.outpass.outpass_service.client.ProfileServiceClient;
import com.outpass.outpass_service.dto.*;
import com.outpass.outpass_service.enums.DutyStatus;
import com.outpass.outpass_service.model.*;
import com.outpass.outpass_service.repo.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class OutpassService {

	private final OutpassRepository outpassRepo;
	private final OutpassApprovalRepository approvalRepo;
	private final OutpassQrRepository qrRepo;
	private final OutpassEmailResendRepository resendRepository;
	private final TokenService tokenService;
	private final AuthServiceClient authServiceClient;
	private final ProfileServiceClient profileServiceClient;
	private final KafkaTemplate<String, OutpassEvent> kafkaTemplate;

	@Transactional
	public String apply(@Valid OutpassRequest outpassRequest, String studentUserId, String role) {
		log.info("Outpass application started: studentUserId={}, outpassType={}", studentUserId,
				outpassRequest.getOutpassType());
		if (!"STUDENT".equalsIgnoreCase(role)) {
			log.warn("Unauthorized outpass application attempt: studentUserId={}, role={}", studentUserId, role);
			throw new UnauthorizedOutpassException("Only students can apply for outpass");
		}
		UserValidationResponse user = authServiceClient.getUser(studentUserId);
		if (user == null) {
			log.warn("User not found while applying for outpass: studentUserId={}", studentUserId);
			throw new UserValidationException("User not found: " + studentUserId);
		}
		if (!user.isEnabled()) {
			log.warn("Disabled user attempted to apply for outpass: studentUserId={}", studentUserId);
			throw new UserValidationException("User account is disabled: " + studentUserId);
		}
		if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
			log.warn("Non-student user attempted student outpass application: studentUserId={}, actualRole={}",
					studentUserId, user.getRole());
			throw new UserValidationException("User " + studentUserId + " is not a STUDENT");
		}
		boolean hasActiveOutpass = outpassRepo.existsByStudentUserIdAndStatusIn(studentUserId,
				List.of(OutpassStatus.PENDING, OutpassStatus.PARENT_APPROVED, OutpassStatus.WARDEN_APPROVED,
						OutpassStatus.OUT));
		if (hasActiveOutpass) {
			log.warn("Student already has an active outpass: studentUserId={}, outpassType={}", studentUserId,
					outpassRequest.getOutpassType());
			throw new OutpassBusinessException("You already have an active " + outpassRequest.getOutpassType());
		}
		LocalDateTime now = LocalDateTime.now();
		if (outpassRequest.getOutTime().isBefore(now)) {
			log.warn("Outpass application rejected because outTime is in the past: studentUserId={}, outTime={}",
					studentUserId, outpassRequest.getOutTime());
			throw new OutpassBusinessException(
					outpassRequest.getOutpassType() + " cannot be applied for the past time");
		}
		if (!outpassRequest.getExpectedInTime().isAfter(outpassRequest.getOutTime())) {
			log.warn("Invalid outpass time range: studentUserId={}, outTime={}, expectedInTime={}", studentUserId,
					outpassRequest.getOutTime(), outpassRequest.getExpectedInTime());
			throw new OutpassBusinessException("Expected-in-time must be after out-time");
		}
		if (outpassRequest.getOutpassType() == OutpassType.OUTING) {
			LocalDate today = LocalDate.now(ZoneId.systemDefault());
			if (!outpassRequest.getOutTime().toLocalDate().equals(today)) {
				throw new OutpassBusinessException("OUTING can be applied for today only");
			}
			if (!outpassRequest.getExpectedInTime().toLocalDate().equals(outpassRequest.getOutTime().toLocalDate())) {
				throw new OutpassBusinessException("OUTING must start and end on the same day");
			}
		}
		if (outpassRequest.getOutpassType() == OutpassType.OUTPASS) {
			if (!outpassRequest.getExpectedInTime().toLocalDate().isAfter(outpassRequest.getOutTime().toLocalDate())) {
				throw new OutpassBusinessException("OUTPASS expected-in date must be after the out date");
			}
		}
		StudentParentResponse parent = profileServiceClient.getStudentParent(studentUserId);
		if (parent == null) {
			log.warn("Student profile not found: studentUserId={}", studentUserId);
			throw new UserValidationException("Student profile not found: " + studentUserId);
		}
		if (parent.getParentEmail() == null || parent.getParentEmail().isBlank()) {
			log.warn("Parent email missing: studentUserId={}", studentUserId);
			throw new UserValidationException("Parent email is not available for student: " + studentUserId);
		}
		List<WardenDutyResponse> duties = profileServiceClient.getTodaysWardenDuties();
		List<WardenDutyResponse> onDutyWardens = duties.stream().filter(duty -> duty.getWardenUserId() != null)
				.filter(duty -> !duty.getWardenUserId().isBlank())
				.filter(duty -> duty.getStatus() == DutyStatus.ON_DUTY).toList();
		if (onDutyWardens.isEmpty()) {
			log.warn("No warden is on duty: studentUserId={}", studentUserId);
			throw new WardenDutyException("No warden is on duty today");
		}
		long assignedCount = approvalRepo.countByApproverTypeAndCreatedAtBetween(ApproverType.WARDEN,
				LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
		WardenDutyResponse selectedDuty = onDutyWardens.get((int) (assignedCount % onDutyWardens.size()));
		String wardenUserId = selectedDuty.getWardenUserId();
		log.info("Warden selected for outpass: studentUserId={}, wardenUserId={}", studentUserId, wardenUserId);
		UserValidationResponse warden = authServiceClient.getUser(wardenUserId);
		if (warden == null) {
			log.error("Assigned warden not found in auth service: wardenUserId={}", wardenUserId);
			throw new WardenDutyException("Assigned warden not found: " + wardenUserId);
		}
		if (!warden.isEnabled()) {
			log.warn("Assigned warden account is disabled: wardenUserId={}", wardenUserId);
			throw new WardenDutyException("Assigned warden account is disabled: " + wardenUserId);
		}
		if (!"WARDEN".equalsIgnoreCase(warden.getRole())) {
			log.error("Invalid role for assigned warden: wardenUserId={}, role={}", wardenUserId, warden.getRole());
			throw new WardenDutyException("Assigned duty user is not a WARDEN: " + wardenUserId);
		}
		if (warden.getEmail() == null || warden.getEmail().isBlank()) {
			log.warn("Assigned warden has no valid email: wardenUserId={}", wardenUserId);
			throw new WardenDutyException("Assigned warden does not have a valid email");
		}
		String parentEmail = parent.getParentEmail();
		String wardenEmail = warden.getEmail();
		String rawToken = tokenService.generateToken();
		String tokenHash = tokenService.hash(rawToken);
		Outpass outpass = new Outpass();
		outpass.setStudentUserId(studentUserId);
		outpass.setOutpassType(outpassRequest.getOutpassType());
		outpass.setReason(outpassRequest.getReason());
		outpass.setDestination(outpassRequest.getDestination());
		outpass.setOutTime(outpassRequest.getOutTime());
		outpass.setExpectedInTime(outpassRequest.getExpectedInTime());
		outpass.setStatus(OutpassStatus.PENDING);
		OutpassApproval parentApproval = new OutpassApproval();
		parentApproval.setApproverType(ApproverType.PARENT);
		parentApproval.setApproverEmail(parentEmail);
		parentApproval.setStatus(ApprovalStatus.PENDING);
		parentApproval.setApprovalTokenHash(tokenHash);
		parentApproval.setApprovalTokenExpiry(LocalDateTime.now().plusHours(24));
		outpass.addApproval(parentApproval);
		outpassRepo.save(outpass);
		log.info("Outpass created successfully: outpassId={}, studentUserId={}, outpassType={}, wardenUserId={}",
				outpass.getId(), studentUserId, outpass.getOutpassType(), wardenUserId);
		OutpassEvent event = new OutpassEvent(outpass.getId(), user.getEmail(), parentEmail, wardenEmail,
				OutpassStatus.PENDING.name(), rawToken);
		kafkaTemplate.send("outpass-events", event);
		log.info("Outpass event published: outpassId={}, studentUserId={}", outpass.getId(), studentUserId);
		return rawToken;
	}

	@Transactional
	public void approveOutpass(Long id, String wardenUserId) {
		log.info("Warden approval started: outpassId={}, wardenUserId={}", id, wardenUserId);
		Outpass outpass = outpassRepo.findById(id).orElseThrow(() -> {
			log.warn("Outpass not found for warden approval: outpassId={}", id);
			return new OutpassNotFoundException("Outpass not found: " + id);
		});
		if (outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
			log.warn("Warden approval rejected due to invalid status: outpassId={}, status={}, wardenUserId={}", id,
					outpass.getStatus(), wardenUserId);
			throw new OutpassBusinessException(
					"Outpass cannot be approved by warden in its current status: " + outpass.getStatus());
		}
		validateTodaysWarden(wardenUserId);
		OutpassApproval wardenApproval = approvalRepo.findByOutpassIdAndApproverType(id, ApproverType.WARDEN)
				.orElseGet(() -> {
					log.debug("Creating warden approval record: outpassId={}, wardenUserId={}", id, wardenUserId);
					OutpassApproval approval = new OutpassApproval();
					approval.setOutpass(outpass);
					approval.setApproverType(ApproverType.WARDEN);
					return approval;
				});
		wardenApproval.setApproverUserId(wardenUserId);
		wardenApproval.setStatus(ApprovalStatus.APPROVED);
		wardenApproval.setActionedAt(LocalDateTime.now());
		outpass.setStatus(OutpassStatus.WARDEN_APPROVED);
		approvalRepo.save(wardenApproval);
		outpassRepo.save(outpass);

		UserValidationResponse student = authServiceClient.getUser(outpass.getStudentUserId());
		log.info("Outpass approved by warden: outpassId={}, studentUserId={}, wardenUserId={}", outpass.getId(),
				outpass.getStudentUserId(), wardenUserId);
		OutpassEvent event = new OutpassEvent(outpass.getId(), student.getEmail(),
				OutpassStatus.WARDEN_APPROVED.name());
		kafkaTemplate.send("outpass-events", event);
		log.info("Warden approval event published: outpassId={}, wardenUserId={}", outpass.getId(), wardenUserId);
	}

	@Transactional
	public void rejectOutpass(Long id, String wardenUserId, String remarks) {
		log.info("Warden rejection started: outpassId={}, wardenUserId={}", id, wardenUserId);
		Outpass outpass = outpassRepo.findById(id).orElseThrow(() -> {
			log.warn("Outpass not found for warden rejection: outpassId={}", id);
			return new OutpassNotFoundException("Outpass not found: " + id);
		});
		if (outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
			log.warn("Warden rejection rejected due to invalid status: outpassId={}, status={}, wardenUserId={}", id,
					outpass.getStatus(), wardenUserId);
			throw new OutpassBusinessException(
					"Outpass cannot be rejected by warden in its current status: " + outpass.getStatus());
		}
		validateTodaysWarden(wardenUserId);
		OutpassApproval wardenApproval = approvalRepo.findByOutpassIdAndApproverType(id, ApproverType.WARDEN)
				.orElseThrow(() -> {
					log.warn("Warden approval assignment not found: outpassId={}", id);
					return new OutpassNotFoundException("Warden approval assignment not found");
				});
		if (!wardenUserId.equals(wardenApproval.getApproverUserId())) {
			log.warn("Unauthorized warden rejection attempt: outpassId={}, assignedWarden={}, requestingWarden={}", id,
					wardenApproval.getApproverUserId(), wardenUserId);
			throw new UnauthorizedOutpassException("You are not assigned to reject this outpass");
		}
		if (wardenApproval.getStatus() != ApprovalStatus.PENDING) {
			log.warn("Warden approval already processed: outpassId={}, status={}", id, wardenApproval.getStatus());
			throw new OutpassBusinessException("Warden has already processed this outpass");
		}
		wardenApproval.setStatus(ApprovalStatus.REJECTED);
		wardenApproval.setActionedAt(LocalDateTime.now());
		wardenApproval.setRemarks(remarks);
		outpass.setStatus(OutpassStatus.REJECTED);
		approvalRepo.save(wardenApproval);
		outpassRepo.save(outpass);

		UserValidationResponse student = authServiceClient.getUser(outpass.getStudentUserId());
		log.info("Outpass rejected by warden: outpassId={}, studentUserId={}, wardenUserId={}", outpass.getId(),
				student.getEmail(), wardenUserId);
		OutpassEvent event = new OutpassEvent(outpass.getId(), outpass.getStudentUserId(),
				OutpassStatus.REJECTED.name());
		kafkaTemplate.send("outpass-events", event);
		log.info("Warden rejection event published: outpassId={}, wardenUserId={}", outpass.getId(), wardenUserId);
	}

	private void validateTodaysWarden(String wardenUserId) {
		List<WardenDutyResponse> duties = profileServiceClient.getTodaysWardenDuties();
		if (duties == null || duties.isEmpty()) {
			throw new WardenDutyException("No warden is assigned for today");
		}
		boolean assigned = duties.stream()
				.anyMatch(duty -> duty.getWardenUserId() != null && !duty.getWardenUserId().isBlank()
						&& duty.getStatus() == DutyStatus.ON_DUTY && wardenUserId.equals(duty.getWardenUserId()));
		if (!assigned) {
			throw new WardenDutyException("You are not assigned as today's warden");
		}
	}

	@Transactional
	public OutpassResponse getOutpass(String studentUserId, String role, Long outpassId) {
		log.debug("Fetching outpass: outpassId={}, studentUserId={}", outpassId, studentUserId);
		if (!"STUDENT".equalsIgnoreCase(role)) {
			log.warn("Unauthorized outpass access attempt: outpassId={}, userId={}, role={}", outpassId, studentUserId,
					role);
			throw new UnauthorizedOutpassException("Only students can access this outpass");
		}
		Outpass outpass = outpassRepo.findById(outpassId).orElseThrow(() -> {
			log.warn("Outpass not found: outpassId={}", outpassId);
			return new OutpassNotFoundException("Outpass not found: " + outpassId);
		});
		if (!outpass.getStudentUserId().equals(studentUserId)) {
			log.warn("Unauthorized outpass access: outpassId={}, ownerUserId={}, requestingUserId={}", outpassId,
					outpass.getStudentUserId(), studentUserId);
			throw new UnauthorizedOutpassException("You are not authorized to view this outpass");
		}
		OutpassResponse response = new OutpassResponse();
		response.setId(outpass.getId());
		response.setDestination(outpass.getDestination());
		response.setOutTime(outpass.getOutTime());
		response.setInTime(outpass.getExpectedInTime());
		response.setOutpassType(outpass.getOutpassType());
		response.setStatus(outpass.getStatus());
		log.debug("Outpass retrieved successfully: outpassId={}, status={}", outpassId, outpass.getStatus());
		return response;
	}

	@Transactional
	public void cancelRequest(Long id, String studentUserId) {
		log.info("Cancelling outpass: outpassId={}, studentUserId={}", id, studentUserId);
		Outpass outpass = outpassRepo.findById(id).orElseThrow(() -> {
			log.warn("Outpass not found for cancellation: outpassId={}", id);
			return new OutpassNotFoundException("Outpass not found: " + id);
		});
		if (!outpass.getStudentUserId().equals(studentUserId)) {
			log.warn("Unauthorized cancellation attempt: outpassId={}, studentUserId={}", id, studentUserId);
			throw new UnauthorizedOutpassException("You are not authorized to cancel this outpass");
		}
		if (outpass.getStatus() != OutpassStatus.PENDING && outpass.getStatus() != OutpassStatus.PARENT_APPROVED) {
			log.warn("Outpass cannot be cancelled: outpassId={}, status={}", id, outpass.getStatus());
			throw new OutpassBusinessException("Cannot cancel outpass at this stage");
		}
		outpass.setStatus(OutpassStatus.CANCELLED);
		outpassRepo.save(outpass);
		log.info("Outpass cancelled successfully: outpassId={}, studentUserId={}", id, studentUserId);
	}

	public List<OutpassResponse> getOutpasses(String studentUserId, String role) {
		if (!"STUDENT".equalsIgnoreCase(role)) {
			log.warn("Unauthorized attempt to access outpass list: userId={}, role={}", studentUserId, role);
			throw new UnauthorizedOutpassException("Only students can access this resource");
		}
		log.debug("Fetching outpass history: studentUserId={}", studentUserId);
		List<Outpass> outpassList = outpassRepo.findAllByStudentUserId(studentUserId);
		List<OutpassResponse> responses = new ArrayList<>();
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
			responses.add(response);
		}
		log.debug("Outpass history retrieved: studentUserId={}, count={}", studentUserId, responses.size());
		return responses;
	}

	public QrResponse getQr(Long id, String studentUserId) {
		log.debug("Fetching QR: outpassId={}, studentUserId={}", id, studentUserId);
		Outpass outpass = outpassRepo.findById(id)
				.orElseThrow(() -> new OutpassNotFoundException("Outpass not found: " + id));
		if (!outpass.getStudentUserId().equals(studentUserId)) {
			log.warn("Unauthorized QR access attempt: outpassId={}, studentUserId={}", id, studentUserId);
			throw new UnauthorizedOutpassException("You are not authorized to access this QR");
		}
		if (outpass.getStatus() != OutpassStatus.WARDEN_APPROVED && outpass.getStatus() != OutpassStatus.OUT) {
			throw new OutpassBusinessException("QR not available for this outpass");
		}
		OutpassQr qr = qrRepo.findByOutpassId(id)
				.orElseThrow(() -> new OutpassNotFoundException("QR not found for this outpass"));
		return new QrResponse(outpass.getId(), qr.getQrToken(), qr.getExpiresAt());
	}

	@Transactional
	public void cancelExpiredOutpasses() {
		LocalDateTime now = LocalDateTime.now();
		List<Outpass> outpasses = outpassRepo.findExpiredOutpasses(now);
		if (outpasses.isEmpty()) {
			log.debug("No expired outpasses found");
			return;
		}
		for (Outpass outpass : outpasses) {
			log.info("Cancelling expired outpass: outpassId={}, studentUserId={}", outpass.getId(),
					outpass.getStudentUserId());
			outpass.setStatus(OutpassStatus.CANCELLED);
		}
		outpassRepo.saveAll(outpasses);
		log.info("Expired outpasses cancelled successfully: count={}", outpasses.size());
	}

	@Transactional
	public ResendEmailResponse resendEmail(Long outpassId, EmailRecipient recipient, Authentication authentication) {
		String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("").replaceFirst("^ROLE_", "");
		if (!"STUDENT".equalsIgnoreCase(role)) {
			log.warn("Unauthorized email resend attempt: outpassId={}, role={}", outpassId, role);
			throw new UnauthorizedOutpassException("Only students can resend outpass emails");
		}
		String studentUserId = authentication.getName();
		if (studentUserId == null || studentUserId.isBlank()) {
			throw new UserValidationException("Authenticated user ID is missing");
		}
		Outpass outpass = outpassRepo.findById(outpassId)
				.orElseThrow(() -> new OutpassNotFoundException("Outpass not found: " + outpassId));
		if (!outpass.getStudentUserId().equals(studentUserId)) {
			throw new UnauthorizedOutpassException("You are not authorized to resend this email");
		}
		OutpassApproval approval = approvalRepo
				.findByOutpassIdAndApproverType(outpassId,
						recipient == EmailRecipient.PARENT ? ApproverType.PARENT : ApproverType.WARDEN)
				.orElseThrow(() -> new OutpassNotFoundException(recipient + " approval record not found"));
		if (approval.getStatus() == ApprovalStatus.APPROVED) {
			throw new EmailResendException(recipient + " has already approved this outpass");
		}
		if (approval.getStatus() == ApprovalStatus.REJECTED) {
			throw new EmailResendException(recipient + " has already rejected this outpass");
		}
		OutpassEmailResend resend = resendRepository.findByOutpass_IdAndRecipient(outpassId, recipient)
				.orElseGet(() -> {
					OutpassEmailResend newResend = new OutpassEmailResend();
					newResend.setOutpass(outpass);
					newResend.setRecipient(recipient);
					newResend.setResendCount(0);
					return resendRepository.save(newResend);
				});
		if (resend.getResendCount() >= 3) {
			throw new EmailResendException("Maximum email resend limit of 3 has been reached");
		}
		resend.setResendCount(resend.getResendCount() + 1);
		resendRepository.save(resend);
		String rawToken = tokenService.generateToken();
		String tokenHash = tokenService.hash(rawToken);
		approval.setApprovalTokenHash(tokenHash);
		approval.setApprovalTokenExpiry(LocalDateTime.now().plusHours(12));
		approval.setStatus(ApprovalStatus.PENDING);
		approvalRepo.save(approval);
		OutpassEvent event;
		if (recipient == EmailRecipient.PARENT) {
			event = new OutpassEvent(outpass.getId(), outpass.getStudentUserId(), approval.getApproverEmail(), null,
					OutpassStatus.PENDING.name(), rawToken);
			kafkaTemplate.send("outpass-parent-events", event);
		} else {
			event = new OutpassEvent(outpass.getId(), outpass.getStudentUserId(), approval.getApproverEmail(), null,
					OutpassStatus.PARENT_APPROVED.name(), null);
			kafkaTemplate.send("outpass-warden-events", event);
		}
		int remainingAttempts = 3 - resend.getResendCount();
		return new ResendEmailResponse("Email resent successfully", recipient, resend.getResendCount(),
				remainingAttempts);
	}
}