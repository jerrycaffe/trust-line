package trustline.appuser.service;

import trustline.appuser.dto.EmailRequest;
import trustline.appuser.dto.OtpModeEnum
import trustline.appuser.dto.VerificationType
import trustline.notification.model.VerificationModel
import java.util.*

interface EmailService {
    fun sendMail(emailRequest: EmailRequest): String?
    fun saveVerification(
        mode: OtpModeEnum,
        messageId: String,
        userId: UUID,
        otp: String,
        type: VerificationType
    ): VerificationModel?

    fun getVerificationById(id: UUID): Optional<VerificationModel>
    fun getbyUserIdAndPin(userId: UUID, pin: String): Optional<VerificationModel>
}
