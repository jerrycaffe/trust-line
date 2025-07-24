package com.trustline.trustline.appuser.service;

import com.mailersend.sdk.emails.Email;
import com.trustline.trustline.appuser.dto.EmailRequest;
import com.trustline.trustline.appuser.model.OtpModeEnum;
import com.trustline.trustline.appuser.model.VerificationModel;
import com.trustline.trustline.appuser.repository.VerificationRepository;
import com.trustline.trustline.config.exception.BadRequestException;
import com.trustline.trustline.config.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.exceptions.MailerSendException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final VerificationRepository verificationRepository;
    @Value("${email.mailerSend.api-key}")
    private String apiKey;

    @Override
    public String sendMail(EmailRequest emailRequest) {
        String messageId = null;
        Email email = new Email();
        email.setFrom("Trustline Group", "admin@trustline.com.ng");
        email.addRecipient(emailRequest.getRecipientName(), emailRequest.getRecipientEmail());

        email.setSubject(emailRequest.getSubject());

        email.setHtml(emailRequest.getHtmlTemplate());

        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);

        try {
            MailerSendResponse response = ms.emails().send(email);
            messageId = response.messageId;
            System.out.println(response.messageId);
        } catch (MailerSendException e) {
            log.error(e.message);
        }
        return messageId;
    }

    @Override
    public VerificationModel saveVerification(OtpModeEnum mode, String messageId, UUID userId, String otp) {
        VerificationModel verificationModel = VerificationModel.builder()
                .pin(otp)
                .mode(OtpModeEnum.EMAIL)
                .messageId(messageId)
                .userId(userId)
                .build();
       return verificationRepository.save(verificationModel);
    }

    @Override
    public Boolean verifyOtp(UUID userId, String pin) {

        VerificationModel verificationModel = verificationRepository.findByUserIdAndPin(userId, pin).orElseThrow(() -> new NotFoundException("No previous activation found for this user"));

//        TODO: check if the time has not expired, if it has exceeded an hour fail verification
        if (verificationModel.getCreatedAt().isAfter(LocalDateTime.now().plusHours(2)))
            throw new BadRequestException("Token expired, initiate another verification");
        return true;
    }


}
