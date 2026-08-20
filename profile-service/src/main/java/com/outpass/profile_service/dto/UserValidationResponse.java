package com.outpass.profile_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserValidationResponse {

    private String userId;
    private String email;
    private String role;
    private boolean enabled;
}