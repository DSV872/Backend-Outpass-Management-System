package com.outpass.outpass_service.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.outpass.outpass_service.model.ApproverType;
import com.outpass.outpass_service.model.OutpassApproval;

public interface OutpassApprovalRepository extends JpaRepository<OutpassApproval, Long> {

	Optional<OutpassApproval> findByApprovalTokenHash(String approvalTokenHash);

	Optional<OutpassApproval> findByOutpassIdAndApproverType(Long outpassId, ApproverType approverType);

	List<OutpassApproval> findByApproverUserIdAndApproverType(String wardenUserId, ApproverType warden);

	long countByApproverTypeAndCreatedAtBetween(ApproverType approverType, LocalDateTime startOfDay,
			LocalDateTime endOfDay);
}