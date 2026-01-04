package trustline.appuser.service;

import com.mailersend.sdk.MailerSend
import com.mailersend.sdk.emails.Email
import com.mailersend.sdk.exceptions.MailerSendException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trustline.appuser.dto.EmailRequest
import trustline.appuser.dto.OtpModeEnum
import trustline.appuser.dto.VerificationType
import trustline.appuser.model.VerificationModel
import trustline.appuser.repository.VerificationRepository
import java.util.*

@Service
class EmailServiceImpl(
    private val verificationRepository: VerificationRepository,
    @Value("\${email.mailerSend.api-key}")
    private val apiKey: String
) : EmailService {

    private val log = KotlinLogging.logger {}
    override fun sendMail(emailRequest: EmailRequest): String? {
        var messageId: String? = null

        val email = Email().apply {
            setFrom("Trustline Group", "admin@trustline.com.ng")
            addRecipient(
                emailRequest.recipientName,
                emailRequest.recipientEmail
            )
            subject = emailRequest.subject
            html = emailRequest.htmlTemplate
        }

        val mailerSend = MailerSend().apply {
            token = apiKey
        }

        try {
            val response = mailerSend.emails().send(email)
            messageId = response.messageId
            println(response.messageId)
        } catch (e: MailerSendException) {
            log.error { e.message }
        }

        return messageId
    }


    override fun saveVerification(
        mode: OtpModeEnum,
        messageId: String,
        userId: UUID,
        otp: String,
        type: VerificationType
    ): VerificationModel? {

        val verificationModel =
            VerificationModel(pin = otp, mode = OtpModeEnum.EMAIL, messageId = messageId, type = type, userId = userId)

        return verificationRepository.save(verificationModel)
    }

    override fun getVerificationById(id: UUID): Optional<VerificationModel> {
        return verificationRepository.findById(id);
    }

    override fun getbyUserIdAndPin(userId: UUID, pin: String): Optional<VerificationModel> {
        return verificationRepository.findByUserIdAndPin(userId, pin);
    }


}
