package com.application.notification_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.notification_service.dto.NotificationRequestDto;
import com.application.notification_service.entity.NotificationEntity;
import com.application.notification_service.service.EmailService;
import com.application.notification_service.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
	
	private final NotificationService service;
	
	private final EmailService emailService;
	
	@PostMapping
	public NotificationEntity sendNotification(@RequestBody NotificationRequestDto request) {
		
		return service.createNotification(request);
		
	}
	
	@GetMapping("/user/{userId}")
	public List<NotificationEntity> getUserNotifications(@PathVariable Long userId){
		
		return service.getUserNotification(userId);
	}
	
	@PutMapping("/{id}/read")
	public String markAsRead(@PathVariable Long id) {
		service.markAsRead(id);
		return "Notification marked as read";
		
	}
	
	@GetMapping("/test-email")
	public String testMail() {
		emailService.sendHtmlEmail(
		        "kotimakkena2004@gmail.com",
		        "Test Mail",
		        "Spring Boot Email Working!");
		return "email snet";
	}
	

	
	

}
