package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.EmailRequest;
import com.trustline.trustline.appuser.model.OtpModeEnum;
import com.trustline.trustline.appuser.model.VerificationModel;

import java.util.UUID;

public interface EmailService {
    String sendMail(EmailRequest emailRequest);

    VerificationModel saveVerification(OtpModeEnum mode, String messageId, UUID userId, String otp);

    Boolean verifyOtp(UUID userId, String verificationId);
}
