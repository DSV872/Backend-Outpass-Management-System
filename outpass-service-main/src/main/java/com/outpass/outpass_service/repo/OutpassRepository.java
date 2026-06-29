package com.outpass.outpass_service.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.outpass.outpass_service.model.Outpass;
import com.outpass.outpass_service.model.OutpassStatus;

@Repository
public interface OutpassRepository extends JpaRepository<Outpass, Long> {
	boolean existsByStudentEmailAndStatusIn(String studentEmail, List<OutpassStatus> statuses);

	@Query(value = "SELECT * FROM outpass WHERE parent_approval_token_hash = :hash", nativeQuery = true)
	Optional<Outpass> findByParentApprovalTokenHash(@Param("hash") String tokenHash);

	Optional<Outpass> findByQrToken(String qrToken);

	List<Outpass> findAllByStatusAndOutTimeBetween(OutpassStatus status, LocalDateTime startTime,
			LocalDateTime endTime);

	List<Outpass> findByWardenEmailAndStatusInAndOutTimeBetween(String wardenEmail, List<OutpassStatus> statuses,
			LocalDateTime start, LocalDateTime end);

	List<Outpass> findAllByWardenEmailAndStatusIn(String wardenEmail, List<OutpassStatus> statuses);

	List<Outpass> findByStatusInOrderByActualOutTimeDesc(List<OutpassStatus> statuses);

	List<Outpass> findAllByStudentEmail(String email);

	@Query("""
			select o from Outpass o
			where outTime <= :today
			and o.status not in (
				com.outpass.outpass_service.model.OutpassStatus.IN,
				com.outpass.outpass_service.model.OutpassStatus.OUT,
				com.outpass.outpass_service.model.OutpassStatus.CANCELLED
			)
			""")
	List<Outpass> findExpiredOutpasses(@Param("today") LocalDateTime date);

}
