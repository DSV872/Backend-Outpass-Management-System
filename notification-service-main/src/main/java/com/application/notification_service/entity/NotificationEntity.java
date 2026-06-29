package com.application.notification_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="notification_dtls")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class NotificationEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@Column(name="user_id",nullable=false)
	private Long userId;
	
	@Column(nullable=false)
	private String title;
	
	@Lob
	@Column(nullable=false)
	private String message;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private NotificationType type;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private NotificationStatus status;
	
	@Column(name="is_read")
	private Boolean is_Read=false;
	
	@Column(name="created_at")
	private LocalDateTime createdAt=LocalDateTime.now();
	
	@Column(name="sent_at")
	private LocalDateTime sentAt;
	
	 

}
