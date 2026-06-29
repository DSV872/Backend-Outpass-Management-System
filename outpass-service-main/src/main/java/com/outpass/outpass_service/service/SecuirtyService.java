package com.outpass.outpass_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.outpass.outpass_service.dto.QrPayload;
import com.outpass.outpass_service.dto.SecurityOutpassDto;
import com.outpass.outpass_service.model.Outpass;
import com.outpass.outpass_service.model.OutpassStatus;
import com.outpass.outpass_service.repo.OutpassRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecuirtyService {

	private static final String SECRET = "6hKvPsRSc+e7Ehkvt44ahqkU5f9ntYfAs2XdTU8d+QE=";
	private final OutpassRepository outpassRepo;
	
	@Transactional
	public void approve(String email, String role, String qrToken) {
		if(!role.equals("SECURITY"))
			throw new IllegalStateException("Access denied");
		Outpass outpass = validateQrToken(qrToken);
		if(outpass.getStatus().equals(OutpassStatus.OUT)) {
			throw new IllegalStateException("Already exited");
		}
		if(!outpass.getStatus().equals(OutpassStatus.WARDEN_APPROVED)) {
			throw new IllegalStateException(outpass.getOutpassType()+" is not allowed to exit");
		}
		outpass.setActualOutTime(LocalDateTime.now());
		outpass.setStatus(OutpassStatus.OUT);
		outpassRepo.save(outpass);
		
	}
	private Outpass validateQrToken(String qrToken) {
		QrPayload payload = validateAndExtract(qrToken);
		Outpass outpass = outpassRepo.findById(payload.getOutpassId())
				.orElseThrow(()-> new RuntimeException("Invalid qr"));
		
		if(outpass.getExpectedInTime().isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("Outpass expired");
		}
		
		return outpass;
	}
	
	private QrPayload validateAndExtract(String qrToken) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
				.build()
				.parseClaimsJws(qrToken)
				.getBody();
		return new QrPayload(claims.get("outpassId",Long.class),claims.get("email",String.class));
	}
	
	@Transactional
	public void scanIn(String email, String role, String qrToken) {
		if(!role.equals("SECURITY"))
			throw new IllegalStateException("Access denied");
		Outpass outpass = validateQrToken(qrToken);
		if(outpass.getStatus() != OutpassStatus.OUT) {
			throw new IllegalStateException("Student has not exited yet");
		}
		outpass.setActualInTime(LocalDateTime.now());
		outpass.setStatus(OutpassStatus.IN);
		outpassRepo.save(outpass);
		
	}
	
	
	public List<SecurityOutpassDto> getSecurityHistory() {
		// TODO Auto-generated method stub
		List<Outpass> outpasses= outpassRepo.findByStatusInOrderByActualOutTimeDesc(
				List.of(
						OutpassStatus.OUT,
						OutpassStatus.IN
						)
				);
		return outpasses.stream()
				.map(this::convertToSecurityDto)
				.toList();
	}
	private SecurityOutpassDto convertToSecurityDto(Outpass outpass) {
		// TODO Auto-generated method stub
		
		
		return SecurityOutpassDto.builder()
				.id(outpass.getId())
				.studentEmail(outpass.getStudentEmail())
				.outpassType(outpass.getOutpassType())
				.outTime(outpass.getOutTime())
				.expectedInTime(outpass.getExpectedInTime())
				.actualIntime(outpass.getActualInTime())
				.actualOutTime(outpass.getActualOutTime())
				.outpassStatus(outpass.getStatus())
				.build();
	}

}
