package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.EmailRequest;

public interface EmailService {
    void sendMail(EmailRequest emailRequest);
}
