package com.outpass.profile_service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.outpass.profile_service.dto.WardenDutyRequest;
import com.outpass.profile_service.dto.WardenDutyResponse;
import com.outpass.profile_service.enums.DutyStatus;
import com.outpass.profile_service.exception.*;
import com.outpass.profile_service.model.*;
import com.outpass.profile_service.repo.*;
import com.outpass.profile_service.repo.WardenProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WardenDutyService {
	private final WardenDutyRepository wardenDutyRepository;
	private final WardenProfileRepository wardenProfileRepository;

	@Transactional
	public WardenDutyResponse assignDuty(WardenDutyRequest request) {
		String userId = request.getWardenUserId();
		LocalDate dutyDate = request.getDutyDate();
		log.info("Assigning warden duty: wardenUserId={}, dutyDate={}", userId, dutyDate);
		if (dutyDate.isBefore(LocalDate.now())) {
			log.warn("Cannot assign warden duty for past date: userId={}, dutyDate={}", userId, dutyDate);
			throw new WardenDutyException("Warden duty cannot be assigned for a past date");
		}
		WardenProfile warden = wardenProfileRepository.findByUserId(userId).orElseThrow(() -> {
			log.warn("Warden profile not found: userId={}", userId);
			return new WardenProfileNotFoundException("Warden profile not found for userId: " + userId);
		});
		boolean alreadyAssigned = wardenDutyRepository.existsByWardenProfile_UserIdAndDutyDateAndStatus(userId,
				dutyDate, DutyStatus.ON_DUTY);
		if (alreadyAssigned) {
			log.warn("Warden already assigned: userId={}, dutyDate={}", userId, dutyDate);
			throw new WardenDutyException("Warden " + userId + " is already assigned for " + dutyDate);
		}
		long assignedWardenCount = wardenDutyRepository.countByDutyDateAndStatus(dutyDate, DutyStatus.ON_DUTY);
		if (assignedWardenCount >= 2) {
			log.warn("Maximum warden limit reached: dutyDate={}, assignedCount={}", dutyDate, assignedWardenCount);
			throw new WardenDutyException("Maximum of 2 wardens can be assigned for " + dutyDate);
		}
		WardenDuty duty = new WardenDuty();
		duty.setWardenProfile(warden);
		duty.setDutyDate(dutyDate);
		duty.setStatus(DutyStatus.ON_DUTY);
		WardenDuty saved = wardenDutyRepository.save(duty);
		log.info("Warden duty assigned successfully: dutyId={}, userId={}, dutyDate={}", saved.getId(), userId,
				dutyDate);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<WardenDutyResponse> getTodaysDuties() {
		LocalDate today = LocalDate.now();
		log.debug("Fetching today's warden duties: date={}", today);
		List<WardenDutyResponse> duties = wardenDutyRepository.findAllByDutyDateAndStatus(today, DutyStatus.ON_DUTY)
				.stream().map(this::toResponse).toList();
		log.debug("Today's warden duties retrieved: date={}, count={}", today, duties.size());
		return duties;
	}

	@Transactional(readOnly = true)
	public List<WardenDutyResponse> getDutiesByDate(LocalDate date) {
		log.debug("Fetching warden duties: date={}", date);
		List<WardenDuty> duties = wardenDutyRepository.findAllByDutyDateAndStatus(date, DutyStatus.ON_DUTY);
		if (duties.isEmpty()) {
			log.warn("No active warden duty found: date={}", date);
			throw new WardenDutyNotFoundException("No warden is assigned for " + date);
		}
		return duties.stream().map(this::toResponse).toList();
	}

	@Transactional
	public WardenDutyResponse updateDuty(Long dutyId, WardenDutyRequest request) {
		log.info("Updating warden duty: dutyId={}, newWardenUserId={}, dutyDate={}", dutyId, request.getWardenUserId(),
				request.getDutyDate());
		WardenDuty duty = wardenDutyRepository.findById(dutyId).orElseThrow(() -> {
			log.warn("Warden duty not found: dutyId={}", dutyId);
			return new WardenDutyNotFoundException("Warden duty not found: " + dutyId);
		});
		if (duty.getStatus() != DutyStatus.ON_DUTY) {
			log.warn("Cannot update inactive warden duty: dutyId={}, status={}", dutyId, duty.getStatus());
			throw new WardenDutyException("Only an ON_DUTY assignment can be updated");
		}
		LocalDate dutyDate = request.getDutyDate();
		if (dutyDate.isBefore(LocalDate.now())) {
			log.warn("Cannot update duty to past date: dutyId={}, dutyDate={}", dutyId, dutyDate);
			throw new WardenDutyException("Warden duty cannot be assigned for a past date");
		}
		String userId = request.getWardenUserId();
		WardenProfile warden = wardenProfileRepository.findByUserId(userId).orElseThrow(() -> {
			log.warn("Warden profile not found: userId={}", userId);
			return new WardenProfileNotFoundException("Warden profile not found for userId: " + userId);
		});
		boolean duplicate = wardenDutyRepository.existsByWardenProfile_UserIdAndDutyDateAndStatusAndIdNot(userId,
				dutyDate, DutyStatus.ON_DUTY, dutyId);
		if (duplicate) {
			log.warn("Warden already assigned for date: userId={}, dutyDate={}", userId, dutyDate);
			throw new WardenDutyException("Warden is already assigned for " + dutyDate);
		}
		boolean dateChanged = !duty.getDutyDate().equals(dutyDate);
		if (dateChanged) {
			long assignedWardenCount = wardenDutyRepository.countByDutyDateAndStatus(dutyDate, DutyStatus.ON_DUTY);
			if (assignedWardenCount >= 2) {
				log.warn("Maximum warden limit reached while updating: dutyDate={}, assignedCount={}", dutyDate,
						assignedWardenCount);
				throw new WardenDutyException("Maximum of 2 wardens can be assigned for " + dutyDate);
			}
		}
		duty.setWardenProfile(warden);
		duty.setDutyDate(dutyDate);
		duty.setStatus(DutyStatus.ON_DUTY);
		WardenDuty updated = wardenDutyRepository.save(duty);
		log.info("Warden duty updated successfully: dutyId={}, userId={}, dutyDate={}", updated.getId(),
				updated.getWardenProfile().getUserId(), updated.getDutyDate());
		return toResponse(updated);
	}

	@Transactional
	public WardenDutyResponse replaceDuty(Long dutyId, WardenDutyRequest request) {
		log.info("Replacing warden duty: dutyId={}, newWardenUserId={}", dutyId, request.getWardenUserId());
		WardenDuty oldDuty = wardenDutyRepository.findById(dutyId).orElseThrow(() -> {
			log.warn("Warden duty not found for replacement: dutyId={}", dutyId);
			return new WardenDutyNotFoundException("Warden duty not found: " + dutyId);
		});
		if (oldDuty.getDutyDate().isBefore(LocalDate.now())) {
			log.warn("Past warden duty cannot be replaced: dutyId={}, dutyDate={}", dutyId, oldDuty.getDutyDate());
			throw new WardenDutyException("Past warden duty cannot be replaced");
		}
		if (oldDuty.getStatus() != DutyStatus.ON_DUTY) {
			log.warn("Only ON_DUTY assignments can be replaced: dutyId={}, status={}", dutyId, oldDuty.getStatus());
			throw new WardenDutyException("Only an ON_DUTY warden can be replaced");
		}
		LocalDate dutyDate = oldDuty.getDutyDate();
		String oldWardenUserId = oldDuty.getWardenProfile().getUserId();
		String newWardenUserId = request.getWardenUserId();
		if (oldWardenUserId.equals(newWardenUserId)) {
			log.warn("Replacement warden is same as current warden: dutyId={}, userId={}", dutyId, newWardenUserId);
			throw new WardenDutyException("Replacement warden must be different from the current warden");
		}
		WardenProfile newWarden = wardenProfileRepository.findByUserId(newWardenUserId).orElseThrow(() -> {
			log.warn("Replacement warden profile not found: userId={}", newWardenUserId);
			return new WardenProfileNotFoundException("Warden profile not found for userId: " + newWardenUserId);
		});
		boolean alreadyAssigned = wardenDutyRepository.existsByWardenProfile_UserIdAndDutyDateAndStatus(newWardenUserId,
				dutyDate, DutyStatus.ON_DUTY);
		if (alreadyAssigned) {
			log.warn("Replacement warden already assigned: userId={}, dutyDate={}", newWardenUserId, dutyDate);
			throw new WardenDutyException("Warden is already assigned for " + dutyDate);
		}
		oldDuty.setStatus(DutyStatus.OFF_DUTY);
		wardenDutyRepository.save(oldDuty);
		WardenDuty newDuty = new WardenDuty();
		newDuty.setWardenProfile(newWarden);
		newDuty.setDutyDate(dutyDate);
		newDuty.setStatus(DutyStatus.ON_DUTY);
		WardenDuty savedDuty = wardenDutyRepository.save(newDuty);
		log.info(
				"Warden duty replaced successfully: oldDutyId={}, oldWarden={}, newDutyId={}, newWarden={}, dutyDate={}",
				oldDuty.getId(), oldWardenUserId, savedDuty.getId(), newWardenUserId, dutyDate);
		return toResponse(savedDuty);
	}

	@Transactional
	public void closePreviousDuties() {
		LocalDate today = LocalDate.now();
		log.info("Closing previous ON_DUTY assignments: beforeDate={}", today);
		List<WardenDuty> previousDuties = wardenDutyRepository.findByDutyDateBeforeAndStatus(today, DutyStatus.ON_DUTY);
		if (previousDuties.isEmpty()) {
			log.debug("No previous ON_DUTY assignments found");
			return;
		}
		for (WardenDuty duty : previousDuties) {
			duty.setStatus(DutyStatus.OFF_DUTY);
			log.info("Previous warden duty closed: dutyId={}, wardenUserId={}, dutyDate={}", duty.getId(),
					duty.getWardenProfile().getUserId(), duty.getDutyDate());
		}
		wardenDutyRepository.saveAll(previousDuties);
		log.info("Previous warden duties closed successfully: count={}", previousDuties.size());
	}

	private WardenDutyResponse toResponse(WardenDuty duty) {
		return new WardenDutyResponse(duty.getId(), duty.getWardenProfile().getUserId(), duty.getDutyDate(),
				duty.getStatus());
	}
}