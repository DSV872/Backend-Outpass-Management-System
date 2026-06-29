package com.application.notification_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.application.notification_service.dto.NotificationRequestDto;
import com.application.notification_service.entity.NotificationEntity;
import com.application.notification_service.entity.NotificationStatus;
import com.application.notification_service.repository.NotificationRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	
	
	private final NotificationRepository notificationRepo;
	
	public NotificationEntity createNotification(NotificationRequestDto request) {
		
		
		NotificationEntity notification=new NotificationEntity();
		
		notification.setUserId(request.getUserId());
		notification.setTitle(request.getTitle());
		notification.setMessage(request.getMessage());
		notification.setType(request.getType());
		notification.setStatus(NotificationStatus.PENDING);
		notification.setCreatedAt(LocalDateTime.now());
		
		return notificationRepo.save(notification);
	}
	
	public List<NotificationEntity> getUserNotification(Long userId){
		return notificationRepo.findByUserId(userId);
	}
	
	public void markAsRead(Long id) {
		NotificationEntity notification=notificationRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Notification not found"));
		
		notification.setIs_Read(true);
		notificationRepo.save(notification);
	}
	
	
	
	
	
	

}
