package com.outpass.outpass_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OutpassEvent {

    private Long outpassId;

    private String studentEmail;

    private String parentEmail;

    private String wardenEmail;

    private String status;

    private String approvalToken;

    public OutpassEvent(
            Long outpassId,
            String studentEmail,
            String status) {

        this.outpassId = outpassId;
        this.studentEmail = studentEmail;
        this.status = status;
    }
}