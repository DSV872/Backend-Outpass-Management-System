package com.outpass.outpass_service.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutpassScheduler {
	private final OutpassService service;
	
//	@Scheduled(cron = "0 0 0 * * *")
	@Scheduled(fixedRate = 30000)
	public void cancelExpiredOutpasses() {
		System.out.println("Running scheduler....");
		service.cancelExpiredOutpasses();
	}
}
