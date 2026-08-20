package com.outpass.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserValidationResponse {

    private String userId;
    private String email;
    private String role;
    private boolean enabled;
}