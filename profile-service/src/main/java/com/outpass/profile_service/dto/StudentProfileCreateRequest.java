package com.outpass.profile_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentProfileCreateRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String department;

    private Integer yearOfStudy;

    private String section;

    @NotBlank
    private String parentName;

    @NotBlank
    private String parentEmail;

    private String parentPhone;
}