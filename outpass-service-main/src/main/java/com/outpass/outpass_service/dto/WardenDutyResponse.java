package com.outpass.outpass_service.dto;

import java.time.LocalDate;

import com.outpass.outpass_service.enums.DutyStatus;

import lombok.Data;

@Data
public class WardenDutyResponse {

    private Long id;
    private String wardenUserId;
    private LocalDate dutyDate;
    private DutyStatus status;
}
