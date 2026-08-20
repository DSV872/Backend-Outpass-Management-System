package com.outpass.profile_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecurityProfileResponse {

    private Long id;

    private String userId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String gateName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}