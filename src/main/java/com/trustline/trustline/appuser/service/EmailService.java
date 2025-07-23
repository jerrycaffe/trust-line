package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.EmailRequest;
import com.trustline.trustline.appuser.model.VerificationModel;

import java.util.UUID;

public interface EmailService {
    void sendMail(EmailRequest emailRequest);

    Boolean verifyOtp(UUID userId, String verificationId);
}
