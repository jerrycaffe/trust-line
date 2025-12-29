package trustline.appuser.service;

import trustline.appuser.model.VerificationModel;

import java.util.Optional;
import java.util.UUID;

public interface EmailService {
    String sendMail(EmailRequest emailRequest);
    VerificationModel saveVerification(OtpModeEnum mode, String messageId, UUID userId, String otp, VerificationType type);
    Optional<VerificationModel> getVerificationById(UUID id);
    Optional<VerificationModel> getbyUserIdAndPin(UUID userId, String pin);
}
