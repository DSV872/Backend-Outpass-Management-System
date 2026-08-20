package com.outpass.profile_service.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;;

@Data
public class WardenDutyRequest {

    @NotBlank
    private String wardenUserId;

    @NotNull
    private LocalDate dutyDate;
}
