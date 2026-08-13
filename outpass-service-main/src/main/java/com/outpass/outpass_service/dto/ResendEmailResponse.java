package com.outpass.outpass_service.dto;

import com.outpass.outpass_service.model.EmailRecipient;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResendEmailResponse {

    private String message;
    private EmailRecipient recipient;
    private int resendCount;
    private int remainingAttempts;
}