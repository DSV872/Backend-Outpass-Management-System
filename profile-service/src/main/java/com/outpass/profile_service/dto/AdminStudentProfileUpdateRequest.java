package com.outpass.profile_service.dto;

import lombok.Data;

@Data
public class AdminStudentProfileUpdateRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String department;
    private Integer yearOfStudy;
    private String section;

    private String parentName;
    private String parentEmail;
    private String parentPhone;
}