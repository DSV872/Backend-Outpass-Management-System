package com.outpass.profile_service.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.outpass.profile_service.enums.DutyStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warden_duties", uniqueConstraints = {
		@UniqueConstraint(name = "uk_warden_duty", columnNames = { "warden_user_id", "duty_date" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WardenDuty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "warden_user_id", referencedColumnName = "user_id", nullable = false)
	private WardenProfile wardenProfile;

	@Column(name = "duty_date", nullable = false)
	private LocalDate dutyDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private DutyStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	public void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}