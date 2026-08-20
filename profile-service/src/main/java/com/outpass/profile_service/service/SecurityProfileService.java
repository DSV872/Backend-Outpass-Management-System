package com.outpass.profile_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.outpass.profile_service.client.AuthServiceClient;
import com.outpass.profile_service.dto.*;
import com.outpass.profile_service.exception.*;
import com.outpass.profile_service.model.SecurityProfile;
import com.outpass.profile_service.repo.SecurityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityProfileService {

	private final SecurityProfileRepository securityProfileRepository;
	private final AuthServiceClient authServiceClient;

	@Transactional
	public SecurityProfileResponse create(SecurityProfileCreateRequest request) {
		String userId = request.getUserId();
		log.info("Creating security profile: userId={}", userId);
		if (userId == null || userId.isBlank()) {
			log.warn("Security profile creation failed: user ID is missing");
			throw new UserValidationException("User ID cannot be null or empty");
		}
		if (securityProfileRepository.existsByUserId(userId)) {
			log.warn("Security profile already exists: userId={}", userId);
			throw new ProfileBusinessException("Security profile already exists for userId: " + userId);
		}
		UserValidationResponse user;
		try {
			user = authServiceClient.getUser(userId);
		} catch (Exception ex) {
			log.error("Failed to validate user with auth-service: userId={}", userId, ex);
			throw new UserValidationException("Unable to validate user: " + userId, ex);
		}
		if (user == null) {
			log.warn("User not found in auth-service: userId={}", userId);
			throw new UserValidationException("User not found: " + userId);
		}
		if (!user.isEnabled()) {
			log.warn("User is disabled: userId={}", userId);
			throw new UserValidationException("User " + userId + " is disabled");
		}
		if (!"SECURITY".equalsIgnoreCase(user.getRole())) {
			log.warn("Invalid role for security profile: userId={}, role={}", userId, user.getRole());
			throw new UserValidationException("User " + userId + " is not a SECURITY user");
		}
		SecurityProfile profile = new SecurityProfile();
		profile.setUserId(userId);
		profile.setFirstName(request.getFirstName());
		profile.setLastName(request.getLastName());
		profile.setPhoneNumber(request.getPhoneNumber());
		profile.setGateName(request.getGateName());
		SecurityProfile saved = securityProfileRepository.save(profile);
		log.info("Security profile created: profileId={}, userId={}", saved.getId(), saved.getUserId());
		return toResponse(saved);
	}

	public SecurityProfileResponse getByUserId(String userId) {
		log.debug("Fetching security profile: userId={}", userId);
		return toResponse(getProfile(userId));
	}

	public List<SecurityProfileResponse> getAll() {
		log.debug("Fetching all security profiles");
		List<SecurityProfileResponse> profiles = securityProfileRepository.findAll().stream().map(this::toResponse)
				.toList();
		log.debug("Security profiles retrieved: count={}", profiles.size());
		return profiles;
	}

	@Transactional
	public SecurityProfileResponse update(String userId, SecurityProfileUpdateRequest request) {
		log.info("Updating security profile: userId={}", userId);
		SecurityProfile existing = getProfile(userId);
		existing.setFirstName(request.getFirstName());
		existing.setLastName(request.getLastName());
		existing.setPhoneNumber(request.getPhoneNumber());
		existing.setGateName(request.getGateName());
		SecurityProfile updated = securityProfileRepository.save(existing);
		log.info("Security profile updated: profileId={}, userId={}", updated.getId(), updated.getUserId());
		return toResponse(updated);
	}

	@Transactional
	public void delete(String userId) {
		log.info("Deleting security profile: userId={}", userId);
		SecurityProfile existing = getProfile(userId);
		securityProfileRepository.delete(existing);
		log.info("Security profile deleted: profileId={}, userId={}", existing.getId(), existing.getUserId());
	}

	private SecurityProfile getProfile(String userId) {
		return securityProfileRepository.findByUserId(userId).orElseThrow(() -> {
			log.warn("Security profile not found: userId={}", userId);
			return new SecurityProfileNotFoundException("Security profile not found for userId: " + userId);
		});
	}

	private SecurityProfileResponse toResponse(SecurityProfile profile) {
		return new SecurityProfileResponse(profile.getId(), profile.getUserId(), profile.getFirstName(),
				profile.getLastName(), profile.getPhoneNumber(), profile.getGateName(), profile.getCreatedAt(),
				profile.getUpdatedAt());
	}
}