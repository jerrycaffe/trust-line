package com.trustline.trustline.appuser.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmailRequest {
    private String recipientName;
    private String recipientEmail;
    private UUID recipientId;

    private String subject;
}
