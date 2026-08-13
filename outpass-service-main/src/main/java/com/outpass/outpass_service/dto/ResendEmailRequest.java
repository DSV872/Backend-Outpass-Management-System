package com.outpass.outpass_service.dto;

import com.outpass.outpass_service.model.EmailRecipient;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendEmailRequest {

    @NotNull
    private EmailRecipient recipient;
}