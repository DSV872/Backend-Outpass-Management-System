package com.application.notification_service.dto;

import com.application.notification_service.entity.NotificationEntity;
import com.application.notification_service.entity.NotificationType;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDto {

	private Long userId;
	private String title;
	private String message;
	private NotificationType type;
	
}
