package com.outpass.profile_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.outpass.profile_service.client.AuthServiceClient;
import com.outpass.profile_service.dto.UserValidationResponse;
import com.outpass.profile_service.dto.WardenProfileCreateRequest;
import com.outpass.profile_service.dto.WardenProfileResponse;
import com.outpass.profile_service.dto.WardenProfileUpdateRequest;
import com.outpass.profile_service.exception.ProfileBusinessException;
import com.outpass.profile_service.exception.UserValidationException;
import com.outpass.profile_service.exception.WardenProfileNotFoundException;
import com.outpass.profile_service.model.WardenProfile;
import com.outpass.profile_service.repo.WardenProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WardenProfileService {

	private final WardenProfileRepository wardenProfileRepository;
	private final AuthServiceClient authServiceClient;

	@Transactional
	public WardenProfileResponse create(WardenProfileCreateRequest request) {
		String userId = request.getUserId();
		log.info("Creating warden profile: userId={}", userId);
		if (userId == null || userId.isBlank()) {
			log.warn("Warden profile creation failed: user ID is missing");
			throw new UserValidationException("User ID cannot be null or empty");
		}
		if (wardenProfileRepository.existsByUserId(userId)) {
			log.warn("Warden profile already exists: userId={}", userId);
			throw new ProfileBusinessException("Warden profile already exists for userId: " + userId);
		}
		UserValidationResponse user;
		try {
			user = authServiceClient.getUser(userId);
		} catch (Exception ex) {
			log.error("Failed to validate warden with auth-service: userId={}", userId, ex);
			throw new UserValidationException("Unable to validate user: " + userId, ex);
		}
		if (user == null) {
			log.warn("User not found in auth-service: userId={}", userId);
			throw new UserValidationException("User not found: " + userId);
		}
		if (!user.isEnabled()) {
			log.warn("Warden account is disabled: userId={}", userId);
			throw new UserValidationException("User " + userId + " is disabled");
		}
		if (!"WARDEN".equalsIgnoreCase(user.getRole())) {
			log.warn("Invalid role for warden profile: userId={}, role={}", userId, user.getRole());
			throw new UserValidationException("User " + userId + " is not a WARDEN");
		}
		WardenProfile profile = new WardenProfile();
		profile.setUserId(userId);
		profile.setFirstName(request.getFirstName());
		profile.setLastName(request.getLastName());
		profile.setPhoneNumber(request.getPhoneNumber());
		profile.setHostelName(request.getHostelName());
		WardenProfile saved = wardenProfileRepository.save(profile);
		log.info("Warden profile created: profileId={}, userId={}", saved.getId(), saved.getUserId());
		return toResponse(saved);
	}

	public WardenProfileResponse getByUserId(String userId) {
		log.debug("Fetching warden profile: userId={}", userId);
		return toResponse(getProfile(userId));
	}

	public List<WardenProfileResponse> getAll() {
		log.debug("Fetching all warden profiles");
		List<WardenProfileResponse> profiles = wardenProfileRepository.findAll().stream().map(this::toResponse)
				.toList();
		log.debug("Warden profiles retrieved: count={}", profiles.size());
		return profiles;
	}

	@Transactional
	public WardenProfileResponse update(String userId, WardenProfileUpdateRequest request) {
		log.info("Updating warden profile: userId={}", userId);
		WardenProfile existing = getProfile(userId);
		existing.setFirstName(request.getFirstName());
		existing.setLastName(request.getLastName());
		existing.setPhoneNumber(request.getPhoneNumber());
		existing.setHostelName(request.getHostelName());
		WardenProfile updated = wardenProfileRepository.save(existing);
		log.info("Warden profile updated: profileId={}, userId={}", updated.getId(), updated.getUserId());
		return toResponse(updated);
	}

	@Transactional
	public void delete(String userId) {
		log.info("Deleting warden profile: userId={}", userId);
		WardenProfile existing = getProfile(userId);
		wardenProfileRepository.delete(existing);
		log.info("Warden profile deleted: profileId={}, userId={}", existing.getId(), existing.getUserId());
	}

	private WardenProfile getProfile(String userId) {
		return wardenProfileRepository.findByUserId(userId).orElseThrow(() -> {
			log.warn("Warden profile not found: userId={}", userId);
			return new WardenProfileNotFoundException("Warden profile not found for userId: " + userId);
		});
	}

	private WardenProfileResponse toResponse(WardenProfile profile) {
		UserValidationResponse user;
		try {
			user = authServiceClient.getUser(profile.getUserId());
		} catch (Exception ex) {
			log.error("Failed to retrieve user details from auth-service: userId={}", profile.getUserId(), ex);
			throw new UserValidationException("Unable to retrieve user details: " + profile.getUserId(), ex);
		}
		if (user == null) {
			log.warn("User not found while building warden profile response: userId={}", profile.getUserId());
			throw new UserValidationException("User not found: " + profile.getUserId());
		}
		return new WardenProfileResponse(profile.getId(), profile.getUserId(), user.getEmail(), profile.getFirstName(),
				profile.getLastName(), profile.getPhoneNumber(), profile.getHostelName(), profile.getCreatedAt(),
				profile.getUpdatedAt());
	}
}