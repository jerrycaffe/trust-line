package com.trustline.trustline.appuser.service;

import com.mailersend.sdk.emails.Email;
import com.trustline.trustline.appuser.Utility;
import com.trustline.trustline.appuser.dto.EmailRequest;
import com.trustline.trustline.appuser.model.OtpModeEnum;
import com.trustline.trustline.appuser.model.VerificationModel;
import com.trustline.trustline.appuser.repository.VerificationRepository;
import com.trustline.trustline.config.exception.BadRequestException;
import com.trustline.trustline.config.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.exceptions.MailerSendException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final VerificationRepository verificationRepository;
    @Value("${email.mailerSend.api-key}")
    private String apiKey;

    @Override
    public void sendMail(EmailRequest emailRequest) {
        Email email = new Email();
        String otp = String.valueOf(Utility.generateSixDigitsNumber());
        email.setFrom("Trustline Group", "jerrycaffe@trustline.com.ng");
        email.addRecipient(emailRequest.getRecipientName(), emailRequest.getRecipientEmail());

        email.setSubject(emailRequest.getSubject());

//        email.setPlain("This is the text content");
        email.setHtml(htmlFormat(emailRequest.getRecipientName(), otp));

        MailerSend ms = new MailerSend();

        ms.setToken(apiKey);

        try {
            MailerSendResponse response = ms.emails().send(email);
            VerificationModel verificationModel = VerificationModel.builder()
                    .pin(otp)
                    .mode(OtpModeEnum.EMAIL)
                    .messageId(response.messageId)
                    .userId(emailRequest.getRecipientId())
                    .build();
            verificationRepository.save(verificationModel);
            System.out.println(response.messageId);
        } catch (MailerSendException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean verifyOtp(UUID userId, String pin) {

        VerificationModel verificationModel = verificationRepository.finByIdAndPin(userId, pin).orElseThrow(() -> new NotFoundException("No previous activation found for this user"));

//        TODO: check if the time has not expired, if it has exceeded an hour fail verification
        if (verificationModel.getCreatedAt().isAfter(LocalDateTime.now().plusHours(2)))
            throw new BadRequestException("Token expired, initiate another verification");
        return true;
    }

    private String htmlFormat(String user, String otp) {
        String htmlContent = """
                <div style='max-width:600px;margin:auto;background-color:#ffffff;border-radius:8px;
                box-shadow:0 0 5px rgba(0,0,0,0.05);padding:30px 20px;font-family:Segoe UI,sans-serif;'>
                  <div style='text-align:center;padding-bottom:10px;'>
                    <h1 style='color:#837AEF;font-size:24px;margin:0;'>Trustlne</h1>
                  </div>
                  <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                    <p>Hello, <strong>%s</strong></p>
                    <p>Thank you for signing up on <strong>Trustlne</strong>! To complete your registration,
                     please verify your email with the OTP below:</p>
                  </div>
                  <div style='text-align:center;background-color:#CD8FFD22;color:#837AEF;
                  font-size:28px;font-weight:bold;letter-spacing:8px;margin:20px auto;padding:15px 25px;
                  border-radius:6px;width:fit-content;'>
                    %s
                  </div>
                  <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                    <p>This code will expire in 10 minutes. If you didn’t request this, you can ignore this message.</p>
                    <p>Welcome to Trustlne — be ensured your security is totalling guarantee.</p>
                  </div>
                  <div style='font-size:13px;color:#777;text-align:center;margin-top:30px;'>
                    &copy; 2025 Trustlne. All rights reserved.
                  </div>
                </div>
                """;


        return String.format(htmlContent, user, otp);

    }
}
