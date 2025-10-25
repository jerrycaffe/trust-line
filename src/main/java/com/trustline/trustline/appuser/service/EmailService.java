package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.EmailRequest;
import com.trustline.trustline.appuser.model.OtpModeEnum;
import com.trustline.trustline.appuser.model.VerificationModel;
import com.trustline.trustline.appuser.model.VerificationType;

import java.util.Optional;
import java.util.UUID;

public interface EmailService {
    String sendMail(EmailRequest emailRequest);
    VerificationModel saveVerification(OtpModeEnum mode, String messageId, UUID userId, String otp, VerificationType type);
    Optional<VerificationModel> getVerificationById(UUID id);
    Optional<VerificationModel> getbyUserIdAndPin(UUID userId, String pin);
}
