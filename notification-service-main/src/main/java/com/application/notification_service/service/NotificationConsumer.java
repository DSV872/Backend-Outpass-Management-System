package com.application.notification_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.application.notification_service.dto.OutpassEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

	private final EmailService emailService;
	private final TemplateEngine templateEngine;
	@KafkaListener(topics = "outpass-events", groupId = "notification-group")
	public void consume(OutpassEvent event) {

		System.out.println("Event received: " + event.getStatus());

		switch (event.getStatus()) {

		case "PENDING" -> sendParentApprovalMail(event);

		case "PARENT_APPROVED" -> sendParentApprovedMail(event);

		case "WARDEN_APPROVED" -> sendWardenApprovedMail(event);

		case "REJECTED" -> sendRejectedMail(event);

		default -> System.out.println("Unknown event status: " + event.getStatus());
		}
	}

	private void sendParentApprovalMail(OutpassEvent event) {

		Context context = new Context();
		context.setVariable("approveUrl","http://localhost:8080/outpass-service/parent/approve?token="+event.getApprovalToken());
		context.setVariable("rejectUrl", "http://localhost:8080/outpass-service/parent/reject?token="+event.getApprovalToken());
		context.setVariable("studentEmail", event.getStudentEmail());
		String html = templateEngine.process("parent-approval", context);
		System.out.println("Sending mail to: " + event.getParentEmail());
		emailService.sendHtmlEmail(event.getParentEmail(), "Alert:Outpass Approval Required", html);
	}

	private void sendParentApprovedMail(OutpassEvent event) {
		Context context = new Context();
		context.setVariable("studentEmail", event.getStudentEmail());
		String html = templateEngine.process("parent-approved", context);
		emailService.sendHtmlEmail(event.getStudentEmail(), "Alert:Outpass Approved", html);
	}

	private void sendWardenApprovedMail(OutpassEvent event) {
		System.out.println("Sending approved mail to the student "+event.getStudentEmail());
		Context context = new Context();
		context.setVariable("studentEmail", event.getStudentEmail());
		String html = templateEngine.process("warden-approved", context);
		emailService.sendHtmlEmail(event.getStudentEmail(), "Alert:Outpass Approved", html);
	}

	private void sendRejectedMail(OutpassEvent event) {
		Context context = new Context();
		context.setVariable("studentEmail", event.getStudentEmail());
		String html = templateEngine.process("rejected", context);
		emailService.sendHtmlEmail(event.getStudentEmail(), "Alert:Outpass Rejected", html);
	}
}