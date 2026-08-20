package com.outpass.outpass_service.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outpasses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Outpass {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Reference to User.id from auth-service.
	 *
	 * This is NOT a JPA relationship because User belongs to another microservice.
	 */
	@Column(name = "student_user_id", nullable = false)
	private String studentUserId;

	@Enumerated(EnumType.STRING)
	@Column(name = "outpass_type", nullable = false, length = 30)
	private OutpassType outpassType;

	@Column(name = "reason", nullable = false, length = 500)
	private String reason;

	@Column(name = "destination", nullable = false, length = 255)
	private String destination;

	@Column(name = "out_time", nullable = false)
	private LocalDateTime outTime;

	@Column(name = "expected_in_time", nullable = false)
	private LocalDateTime expectedInTime;

	@Column(name = "actual_out_time")
	private LocalDateTime actualOutTime;

	@Column(name = "actual_in_time")
	private LocalDateTime actualInTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private OutpassStatus status;

	@OneToMany(mappedBy = "outpass", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OutpassApproval> approvals = new ArrayList<>();

	@OneToOne(mappedBy = "outpass", cascade = CascadeType.ALL, orphanRemoval = true)
	private OutpassQr qr;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public void addApproval(OutpassApproval approval) {
		approvals.add(approval);
		approval.setOutpass(this);
	}

	public void removeApproval(OutpassApproval approval) {
		approvals.remove(approval);
		approval.setOutpass(null);
	}

	public void setQr(OutpassQr qr) {
		this.qr = qr;

		if (qr != null) {
			qr.setOutpass(this);
		}
	}
}