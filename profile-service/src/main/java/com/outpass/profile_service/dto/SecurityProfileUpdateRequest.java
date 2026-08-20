package com.outpass.profile_service.dto;

import lombok.Data;

@Data
public class SecurityProfileUpdateRequest {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String gateName;
}