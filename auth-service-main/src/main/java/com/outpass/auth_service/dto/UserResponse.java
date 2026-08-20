package com.outpass.auth_service.dto;

import com.outpass.auth_service.model.RoleType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private String userId;
    private String email;
    private RoleType role;
    private boolean enabled;
}