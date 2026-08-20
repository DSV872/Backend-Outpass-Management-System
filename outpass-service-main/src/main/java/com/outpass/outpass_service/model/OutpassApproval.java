package com.outpass.outpass_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outpass_approvals", uniqueConstraints = {
		@UniqueConstraint(name = "uk_outpass_approval_type", columnNames = { "outpass_id", "approver_type" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutpassApproval {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "outpass_id", nullable = false, foreignKey = @ForeignKey(name = "fk_approval_outpass"))
	private Outpass outpass;

	@Enumerated(EnumType.STRING)
	@Column(name = "approver_type", nullable = false, length = 20)
	private ApproverType approverType;

	/**
	 * Used for authenticated approvers such as wardens.
	 *
	 * For a parent who is not an authenticated user, this can remain null.
	 */
	@Column(name = "approver_user_id", length = 20)
	private String approverUserId;

	/**
	 * Parent contact information.
	 *
	 * Can also be used for other non-authenticated approvers.
	 */
	@Column(name = "approver_email", length = 150)
	private String approverEmail;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ApprovalStatus status;

	@Column(name = "actioned_at")
	private LocalDateTime actionedAt;

	@Column(name = "remarks", length = 500)
	private String remarks;

	@Column(name = "signature_url", length = 500)
	private String signatureUrl;

	/**
	 * Used primarily for parent approval links. Store only the hash, never the raw
	 * token.
	 */
	@Column(name = "approval_token_hash", length = 255)
	private String approvalTokenHash;

	@Column(name = "approval_token_expiry")
	private LocalDateTime approvalTokenExpiry;

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
}