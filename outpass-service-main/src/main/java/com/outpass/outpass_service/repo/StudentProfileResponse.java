package com.outpass.outpass_service.repo;

import lombok.Data;

@Data
public class StudentProfileResponse {

    private String userId;
    private String parentName;
    private String parentEmail;
}
