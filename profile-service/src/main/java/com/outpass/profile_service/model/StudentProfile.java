package com.outpass.profile_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Logical reference to auth-service.users.user_id. Example: STD20199
	 *
	 * No JPA relationship and no database foreign key.
	 */
	@Column(name = "user_id", nullable = false, unique = true, length = 20)
	private String userId;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", length = 100)
	private String lastName;

	@Column(name = "phone_number", length = 20)
	private String phoneNumber;

	@Column(name = "department", length = 100)
	private String department;

	@Column(name = "year_of_study")
	private Integer yearOfStudy;

	@Column(name = "section", length = 20)
	private String section;

	@Column(name = "parent_name", nullable = false, length = 150)
	private String parentName;

	@Column(name = "parent_email", nullable = false, length = 100)
	private String parentEmail;

	@Column(name = "parent_phone", length = 20)
	private String parentPhone;

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