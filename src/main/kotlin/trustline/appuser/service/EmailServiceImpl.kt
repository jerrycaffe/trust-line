package trustline.appuser.service;

import com.resend.Resend
import com.resend.core.exception.ResendException
import com.resend.services.emails.model.SendEmailRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trustline.appuser.dto.EmailRequest
import trustline.appuser.dto.OtpModeEnum
import trustline.appuser.dto.Status
import trustline.appuser.dto.VerificationType
import trustline.appuser.model.UserModel
import trustline.appuser.repository.VerificationRepository
import trustline.notification.model.VerificationModel
import java.util.*


@Service
class EmailServiceImpl(
    private val verificationRepository: VerificationRepository,
    @Value("\${email.resend.api-key}")
    private val apiKey: String
) : EmailService {

    private val log = KotlinLogging.logger {}
    override fun sendMail(emailRequest: EmailRequest): String? {
        val resend = Resend(apiKey)

        val params = SendEmailRequest.builder()
            .from("admin@trustline.com.ng")
            .to(emailRequest.recipientEmail)
            .subject(emailRequest.subject)
            .html(emailRequest.htmlTemplate)
            .build()

        var messageId: String? = null


        try {
            val resendResponse = resend.emails().send(params)
            messageId = resendResponse.id
        } catch (e: ResendException) {
            log.error { "Unable to send email due to ${e.message}" }
        }

        return messageId
    }


    override fun saveVerification(
        mode: OtpModeEnum,
        messageId: String,
        user: UserModel,
        otp: String,
        type: VerificationType,
        status: Status
    ): VerificationModel? {

        val verificationModel =
            VerificationModel(
                pin = otp,
                mode = OtpModeEnum.EMAIL,
                messageId = messageId,
                type = type,
                user = user,
                status = status
            )

        return verificationRepository.save(verificationModel)
    }

    override fun getVerificationById(id: UUID): Optional<VerificationModel> {
        return verificationRepository.findById(id);
    }

    override fun getVerificationByIdAndStatus(id: UUID, status: Status): VerificationModel? {
        return verificationRepository.findByIdAndStatus(id, status)
    }

    override fun getbyUserIdAndPinAndStatus(userId: UUID, pin: String, status: Status): VerificationModel? {
        return verificationRepository.findByUserIdAndPinAndStatus(userId, pin, status);
    }

    override fun updateVerification(verificationModel: VerificationModel): VerificationModel? {
        return verificationRepository.save(verificationModel)
    }


}
