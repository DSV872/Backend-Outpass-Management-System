package com.outpass.profile_service.dto;

import java.time.LocalDate;

import com.outpass.profile_service.enums.DutyStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WardenDutyResponse {

    private Long id;
    private String wardenUserId;
    private LocalDate dutyDate;
    private DutyStatus status;
}