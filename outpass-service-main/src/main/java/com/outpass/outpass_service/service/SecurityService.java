package com.outpass.outpass_service.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import com.outpass.outpass_service.dto.*;
import com.outpass.outpass_service.exception.*;
import com.outpass.outpass_service.model.*;
import com.outpass.outpass_service.repo.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

	private final OutpassRepository outpassRepo;
	private final OutpassQrRepository qrRepo;
	private final String qrSecret = "6hKvPsRSc+e7Ehkvt44ahqkU5f9ntYfAs2XdTU8d+QE=";

	@Transactional
	public void approve(String role, String qrToken) {
		log.info("Security scan-out request received");
		validateSecurityRole(role);
		Outpass outpass = validateQrToken(qrToken);
		if (outpass.getStatus() == OutpassStatus.OUT) {
			log.warn("Student has already exited: outpassId={}", outpass.getId());
			throw new OutpassBusinessException("Student has already exited");
		}
		if (outpass.getStatus() != OutpassStatus.WARDEN_APPROVED) {
			log.warn("Outpass is not allowed to exit: outpassId={}, status={}", outpass.getId(), outpass.getStatus());
			throw new OutpassBusinessException(outpass.getOutpassType() + " is not allowed to exit");
		}
		outpass.setActualOutTime(LocalDateTime.now());
		outpass.setStatus(OutpassStatus.OUT);
		outpassRepo.save(outpass);
		log.info("Student exited successfully: outpassId={}, studentUserId={}", outpass.getId(),
				outpass.getStudentUserId());
	}

	@Transactional
	public void scanIn(String role, String qrToken) {
		log.info("Security scan-in request received");
		validateSecurityRole(role);
		Outpass outpass = validateQrToken(qrToken);
		if (outpass.getStatus() != OutpassStatus.OUT) {
			log.warn("Student has not exited: outpassId={}, status={}", outpass.getId(), outpass.getStatus());
			throw new OutpassBusinessException("Student has not exited yet");
		}
		outpass.setActualInTime(LocalDateTime.now());
		outpass.setStatus(OutpassStatus.IN);
		outpassRepo.save(outpass);
		log.info("Student entered successfully: outpassId={}, studentUserId={}", outpass.getId(),
				outpass.getStudentUserId());
	}

	private void validateSecurityRole(String role) {
		if (!"SECURITY".equalsIgnoreCase(role)) {
			log.warn("Unauthorized security operation: role={}", role);
			throw new UnauthorizedOutpassException("Access denied. SECURITY role required");
		}
	}

	private Outpass validateQrToken(String qrToken) {
		if (qrToken == null || qrToken.isBlank()) {
			log.warn("Empty QR token received");
			throw new OutpassBusinessException("QR token cannot be empty");
		}
		QrPayload payload = validateAndExtract(qrToken);
		Long outpassId = payload.getOutpassId();
		Outpass outpass = outpassRepo.findById(outpassId).orElseThrow(() -> {
			log.warn("Outpass not found from QR: outpassId={}", outpassId);
			return new OutpassNotFoundException("Outpass not found: " + outpassId);
		});
		OutpassQr qr = qrRepo.findByOutpassId(outpassId).orElseThrow(() -> {
			log.warn("QR record not found: outpassId={}", outpassId);
			return new OutpassNotFoundException("QR code not found for outpass: " + outpassId);
		});
		if (!qr.getQrToken().equals(qrToken)) {
			log.warn("QR token does not match stored QR: outpassId={}", outpassId);
			throw new OutpassBusinessException("Invalid QR code");
		}
		if (qr.getRevokedAt() != null) {
			log.warn("Revoked QR code scanned: outpassId={}", outpassId);
			throw new OutpassBusinessException("QR code has been revoked");
		}
		if (qr.getExpiresAt() != null && qr.getExpiresAt().isBefore(LocalDateTime.now())) {
			log.warn("Expired QR code scanned: outpassId={}", outpassId);
			throw new OutpassBusinessException("QR code has expired");
		}
		if (outpass.getExpectedInTime().isBefore(LocalDateTime.now())) {
			log.warn("Expired outpass scanned: outpassId={}, expectedInTime={}", outpassId,
					outpass.getExpectedInTime());
			throw new OutpassBusinessException("Outpass has expired");
		}
		return outpass;
	}

	private QrPayload validateAndExtract(String qrToken) {
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(qrSecret)))
					.build().parseClaimsJws(qrToken).getBody();
			Long outpassId = claims.get("outpassId", Long.class);
			if (outpassId == null) {
				log.warn("QR token does not contain outpass ID");
				throw new OutpassBusinessException("QR token does not contain outpass ID");
			}
			return new QrPayload(outpassId);
		} catch (OutpassBusinessException ex) {
			throw ex;
		} catch (Exception ex) {
			log.warn("Invalid QR token received");
			throw new OutpassBusinessException("Invalid QR token");
		}
	}

	@Transactional
	public List<SecurityOutpassDto> getSecurityHistory(String role) {
		validateSecurityRole(role);
		log.debug("Fetching security outpass history");
		List<Outpass> outpasses = outpassRepo
				.findByStatusInOrderByActualOutTimeDesc(List.of(OutpassStatus.OUT, OutpassStatus.IN));
		log.debug("Security history retrieved: count={}", outpasses.size());
		return outpasses.stream().map(this::convertToSecurityDto).toList();
	}

	private SecurityOutpassDto convertToSecurityDto(Outpass outpass) {
		return SecurityOutpassDto.builder().id(outpass.getId()).studentUserId(outpass.getStudentUserId())
				.outpassType(outpass.getOutpassType()).outTime(outpass.getOutTime())
				.expectedInTime(outpass.getExpectedInTime()).actualIntime(outpass.getActualInTime())
				.actualOutTime(outpass.getActualOutTime()).outpassStatus(outpass.getStatus()).build();
	}
}