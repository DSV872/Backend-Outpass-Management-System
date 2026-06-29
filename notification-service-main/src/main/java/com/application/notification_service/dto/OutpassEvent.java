package com.application.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OutpassEvent {

    private Long outpassId;
    private String studentEmail;
    private String parentEmail;
    private String status;
    private String approvalToken;
}