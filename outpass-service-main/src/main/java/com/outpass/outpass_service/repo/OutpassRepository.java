package com.outpass.outpass_service.repo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.outpass.outpass_service.model.ApprovalStatus;
import com.outpass.outpass_service.model.ApproverType;
import com.outpass.outpass_service.model.Outpass;
import com.outpass.outpass_service.model.OutpassStatus;

@Repository
public interface OutpassRepository extends JpaRepository<Outpass, Long> {

	boolean existsByStudentUserIdAndStatusIn(String studentUserId, Collection<OutpassStatus> statuses);

	List<Outpass> findAllByStudentUserId(String studentUserId);

	List<Outpass> findAllByStatusAndOutTimeBetween(OutpassStatus status, LocalDateTime startTime,
			LocalDateTime endTime);

	List<Outpass> findByStatusInOrderByActualOutTimeDesc(List<OutpassStatus> statuses);

	@Query("""
			    SELECT o
			    FROM Outpass o
			    WHERE o.outTime <= :today
			    AND o.status NOT IN (
			        com.outpass.outpass_service.model.OutpassStatus.IN,
			        com.outpass.outpass_service.model.OutpassStatus.OUT,
			        com.outpass.outpass_service.model.OutpassStatus.CANCELLED
			    )
			""")
	List<Outpass> findExpiredOutpasses(@Param("today") LocalDateTime today);

	List<Outpass> findByStatusAndOutTimeBetweenAndApprovals_ApproverTypeAndApprovals_ApproverUserIdAndApprovals_Status(
			OutpassStatus status, LocalDateTime startOfDay, LocalDateTime endOfDay, ApproverType approverType,
			String approverUserId, ApprovalStatus approvalStatus);
}