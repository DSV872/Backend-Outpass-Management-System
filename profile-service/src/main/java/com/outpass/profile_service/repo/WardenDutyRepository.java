package com.outpass.profile_service.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.outpass.profile_service.enums.DutyStatus;
import com.outpass.profile_service.model.WardenDuty;

@Repository
public interface WardenDutyRepository extends JpaRepository<WardenDuty, Long> {

	List<WardenDuty> findByDutyDateAndStatus(LocalDate dutyDate, DutyStatus status);

	List<WardenDuty> findAllByDutyDateAndStatus(LocalDate dutyDate, DutyStatus status);

	long countByDutyDateAndStatus(LocalDate dutyDate, DutyStatus status);

	List<WardenDuty> findByDutyDateBeforeAndStatus(LocalDate today, DutyStatus onDuty);

	boolean existsByWardenProfile_UserIdAndDutyDateAndStatus(String userId, LocalDate dutyDate, DutyStatus onDuty);

	boolean existsByWardenProfile_UserIdAndDutyDateAndStatusAndIdNot(String wardenUserId, LocalDate dutyDate,
			DutyStatus onDuty, Long dutyId);
}