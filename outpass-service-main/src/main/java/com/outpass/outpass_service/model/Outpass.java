package com.outpass.outpass_service.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outpass")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Outpass {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "student_email",nullable = false)
	private String studentEmail;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "outpass_type",nullable=false)
	private OutpassType outpassType;
	
	@Column(name = "reason",nullable = false)
	private String reason;
	
	@Column(name = "destination",nullable = false)
	private String destination;
	
	@Column(name = "out_time",nullable = false)
	private LocalDateTime outTime;
	
	@Column(name = "expected_in_time",nullable = false)
	private LocalDateTime expectedInTime;
	
	@Column(name = "actual_out_time",nullable = true)
	private LocalDateTime actualOutTime;
	
	@Column(name = "actual_in_time",nullable = true)
	private LocalDateTime actualInTime;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status",nullable = false)
	private OutpassStatus status;
	
	@Column(name="parent_email",nullable = false)
	private String parentEmail;
	
	@Column(name = "parent_approved_at",nullable = true)
	private LocalDateTime parentApprovedAt;
	
	@Column(name = "parent_approval_token_hash",nullable = true)
	private String parentApprovalTokenHash;
	
	@Column(name = "parent_approval_token_expiry", nullable = true)
	private LocalDateTime parentApprovalTokenExpiry;
	
	@Column(name = "warden_approved_at",nullable = true)
	private LocalDateTime wardenApprovedAt;
	
	@Column(name = "warden_email",nullable = true)
	private String wardenEmail;
	
	@Column(name = "warden_signature_url",nullable = true)
	private String wardenSignatureUrl;
	
	@Column(name = "warden_remarks",nullable = true)
	private String wardenRemarks;
	
	@Column(name = "qr_token", nullable = true)
	private String qrToken;
	
	@Column(name = "qr_generated_at",nullable = true)
	private LocalDateTime qrGeneratedAt;
	
	@Column(name = "created_at",nullable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at",nullable = true)
	private LocalDateTime updatedAt;
	
}
