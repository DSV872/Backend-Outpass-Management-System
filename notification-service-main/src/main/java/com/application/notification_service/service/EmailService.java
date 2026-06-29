package com.application.notification_service.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;


    public void sendHtmlEmail(String to, String subject, String body) {

    	try {
    		MimeMessage message=mailSender.createMimeMessage();
    		MimeMessageHelper helper= new MimeMessageHelper(message,true);
    		helper.setTo(to);
    		helper.setSubject(subject);
    		helper.setText(body,true);
    		mailSender.send(message);
    	}
        catch(Exception e) {
        	e.printStackTrace();
        }
    }
}