package com.outpass.profile_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentProfileResponse {

    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String department;
    private Integer yearOfStudy;
    private String section;
    private String parentName;
    private String parentEmail;
    private String parentPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}