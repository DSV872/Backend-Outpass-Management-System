package com.outpass.profile_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WardenProfileResponse {

    private Long id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String hostelName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}