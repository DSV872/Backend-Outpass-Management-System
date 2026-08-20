package com.outpass.outpass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserValidationResponse {

    private String userId;
    private String email;
    private String role;
    private boolean enabled;
}