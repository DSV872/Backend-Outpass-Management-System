package com.outpass.profile_service.dto;

import lombok.Data;

@Data
public class WardenProfileUpdateRequest {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String hostelName;
}