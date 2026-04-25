package trustline.appuser.service;

import trustline.appuser.dto.EmailRequest;
import trustline.appuser.dto.OtpModeEnum
import trustline.appuser.dto.Status
import trustline.appuser.dto.VerificationType
import trustline.appuser.model.UserModel
import trustline.notification.model.VerificationModel
import java.util.*

interface EmailService {
    fun sendMail(emailRequest: EmailRequest): String?
    fun saveVerification(
        mode: OtpModeEnum,
        messageId: String,
        user: UserModel,
        otp: String,
        type: VerificationType,
        status: Status
    ): VerificationModel?

    fun getVerificationById(id: UUID): Optional<VerificationModel>
    fun getVerificationByIdAndStatus(id: UUID, status: Status): VerificationModel?
    fun getbyUserIdAndPinAndStatus(userId: UUID, pin: String, status: Status): VerificationModel?
    fun updateVerification(verificationModel: VerificationModel): VerificationModel?
}
