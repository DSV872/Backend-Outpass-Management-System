package com.outpass.profile_service.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WardenDutyScheduler {

    private final WardenDutyService wardenDutyService;

    @Scheduled(cron = "0 0 0 * * *")
    public void closePreviousDuties() {

        wardenDutyService.closePreviousDuties();
    }
}