package com.outpass.profile_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SecurityProfileCreateRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String firstName;

    private String lastName;

    private String phoneNumber;

    @NotBlank
    private String gateName;
}