package com.outpass.outpass_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OUTPASS_EMAIL_RESENDS", uniqueConstraints = {
		@UniqueConstraint(name = "UK_OUTPASS_RECIPIENT", columnNames = { "OUTPASS_ID", "RECIPIENT" }) })
@Getter
@Setter
@NoArgsConstructor
public class OutpassEmailResend {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "OUTPASS_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_EMAIL_RESEND_OUTPASS"))
	private Outpass outpass;

	@Enumerated(EnumType.STRING)
	@Column(name = "RECIPIENT", nullable = false)
	private EmailRecipient recipient;

	@Column(name = "RESEND_COUNT", nullable = false)
	private Integer resendCount = 0;

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATED_AT", nullable = false)
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