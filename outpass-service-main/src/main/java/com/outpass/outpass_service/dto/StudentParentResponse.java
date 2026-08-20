package com.outpass.outpass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentParentResponse {

    private String userId;
    private String parentName;
    private String parentEmail;
    private String parentPhone;
}