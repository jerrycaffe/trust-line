package com.trustline.trustline.appuser.dto;

import lombok.Data;

@Data
public class EmailRequest {
    private String recipientName;
    private String recipientEmail;
    private String subject;
}
