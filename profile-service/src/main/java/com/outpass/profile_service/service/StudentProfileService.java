package com.outpass.profile_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.outpass.profile_service.client.AuthServiceClient;
import com.outpass.profile_service.dto.*;
import com.outpass.profile_service.exception.*;
import com.outpass.profile_service.model.StudentProfile;
import com.outpass.profile_service.repo.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileService {
	private final StudentProfileRepository studentProfileRepository;
	private final AuthServiceClient authServiceClient;

	@Transactional
	public StudentProfileResponse create(StudentProfileCreateRequest request) {
		String userId = request.getUserId();
		log.info("Creating student profile: userId={}", userId);
		if (userId == null || userId.isBlank()) {
			log.warn("Student profile creation failed: user ID is missing");
			throw new UserValidationException("User ID cannot be null or empty");
		}
		if (request.getParentName() == null || request.getParentName().isBlank()) {
			log.warn("Student profile creation failed: parent name is missing, userId={}", userId);
			throw new ProfileBusinessException("Parent name cannot be null or empty");
		}
		if (request.getParentEmail() == null || request.getParentEmail().isBlank()) {
			log.warn("Student profile creation failed: parent email is missing, userId={}", userId);
			throw new ProfileBusinessException("Parent email cannot be null or empty");
		}
		UserValidationResponse user;
		try {
			user = authServiceClient.getUser(userId);
		} catch (Exception ex) {
			log.error("Failed to validate student with auth-service: userId={}", userId, ex);
			throw new UserValidationException("Unable to validate user: " + userId, ex);
		}
		if (user == null) {
			log.warn("User not found in auth-service: userId={}", userId);
			throw new UserValidationException("User not found: " + userId);
		}
		if (!user.isEnabled()) {
			log.warn("Student account is disabled: userId={}", userId);
			throw new UserValidationException("User account is disabled: " + userId);
		}
		if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
			log.warn("Invalid role for student profile: userId={}, role={}", userId, user.getRole());
			throw new UserValidationException("User is not a STUDENT: " + userId);
		}
		if (studentProfileRepository.existsByUserId(userId)) {
			log.warn("Student profile already exists: userId={}", userId);
			throw new ProfileBusinessException("Student profile already exists for userId: " + userId);
		}
		StudentProfile profile = new StudentProfile();
		profile.setUserId(userId);
		profile.setFirstName(request.getFirstName());
		profile.setLastName(request.getLastName());
		profile.setPhoneNumber(request.getPhoneNumber());
		profile.setDepartment(request.getDepartment());
		profile.setYearOfStudy(request.getYearOfStudy());
		profile.setSection(request.getSection());
		profile.setParentName(request.getParentName());
		profile.setParentEmail(request.getParentEmail());
		profile.setParentPhone(request.getParentPhone());
		StudentProfile saved = studentProfileRepository.save(profile);
		log.info("Student profile created: profileId={}, userId={}", saved.getId(), saved.getUserId());
		return toResponse(saved);
	}

	public StudentProfileResponse getByUserId(String userId) {
		log.debug("Fetching student profile: userId={}", userId);
		return toResponse(getProfile(userId));
	}

	public List<StudentProfileResponse> getAll() {
		log.debug("Fetching all student profiles");
		List<StudentProfileResponse> profiles = studentProfileRepository.findAll().stream().map(this::toResponse)
				.toList();
		log.debug("Student profiles retrieved: count={}", profiles.size());
		return profiles;
	}

	@Transactional
	public StudentProfileResponse updateByStudent(String userId, StudentProfileUpdateRequest request) {
		log.info("Student updating own profile: userId={}", userId);
		StudentProfile existing = getProfile(userId);
		existing.setFirstName(request.getFirstName());
		existing.setLastName(request.getLastName());
		existing.setPhoneNumber(request.getPhoneNumber());
		existing.setDepartment(request.getDepartment());
		existing.setYearOfStudy(request.getYearOfStudy());
		existing.setSection(request.getSection());
		StudentProfile updated = studentProfileRepository.save(existing);
		log.info("Student profile updated by student: profileId={}, userId={}", updated.getId(), updated.getUserId());
		return toResponse(updated);
	}

	@Transactional
	public StudentProfileResponse updateByAdmin(String userId, AdminStudentProfileUpdateRequest request) {
		log.info("Admin updating student profile: userId={}", userId);
		StudentProfile existing = getProfile(userId);
		existing.setFirstName(request.getFirstName());
		existing.setLastName(request.getLastName());
		existing.setPhoneNumber(request.getPhoneNumber());
		existing.setDepartment(request.getDepartment());
		existing.setYearOfStudy(request.getYearOfStudy());
		existing.setSection(request.getSection());
		existing.setParentName(request.getParentName());
		existing.setParentEmail(request.getParentEmail());
		existing.setParentPhone(request.getParentPhone());
		StudentProfile updated = studentProfileRepository.save(existing);
		log.info("Student profile updated by admin: profileId={}, userId={}", updated.getId(), updated.getUserId());
		return toResponse(updated);
	}

	@Transactional
	public void delete(String userId) {
		log.info("Deleting student profile: userId={}", userId);
		StudentProfile existing = getProfile(userId);
		studentProfileRepository.delete(existing);
		log.info("Student profile deleted: profileId={}, userId={}", existing.getId(), existing.getUserId());
	}

	public StudentProfileResponse getInternalStudentProfile(String userId) {
		log.debug("Fetching internal student profile: userId={}", userId);
		return getByUserId(userId);
	}

	@Transactional(readOnly = true)
	public StudentParentResponse getStudentParent(String userId) {
		log.debug("Fetching parent details for student: userId={}", userId);
		StudentProfile profile = getProfile(userId);
		return new StudentParentResponse(profile.getUserId(), profile.getParentName(), profile.getParentEmail(),
				profile.getParentPhone());
	}

	private StudentProfile getProfile(String userId) {
		return studentProfileRepository.findByUserId(userId).orElseThrow(() -> {
			log.warn("Student profile not found: userId={}", userId);
			return new StudentProfileNotFoundException("Student profile not found for userId: " + userId);
		});
	}

	private StudentProfileResponse toResponse(StudentProfile profile) {
		return new StudentProfileResponse(profile.getId(), profile.getUserId(), profile.getFirstName(),
				profile.getLastName(), profile.getPhoneNumber(), profile.getDepartment(), profile.getYearOfStudy(),
				profile.getSection(), profile.getParentName(), profile.getParentEmail(), profile.getParentPhone(),
				profile.getCreatedAt(), profile.getUpdatedAt());
	}
}