package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.Utility;
import com.trustline.trustline.appuser.dto.CreateUserRes;
import com.trustline.trustline.appuser.dto.EmailRequest;
import com.trustline.trustline.appuser.dto.RegisterUserDto;
import com.trustline.trustline.appuser.model.*;
import com.trustline.trustline.appuser.repository.UserRepository;
import com.trustline.trustline.config.exception.DuplicateException;
import com.trustline.trustline.config.exception.PhoneNumberAlreadyExistsException;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;



    RegisterUserDto registerReq() {
        return RegisterUserDto.builder()
                .email("test@test.com")
                .password("test1234")
                .phoneNumber("08088492993")
                .build();
    }

    User dbUser() {
        return User.builder()
                .email("test@test.com")
                .phoneNumber("08088492993")
                .status(Status.OTP_VALIDATION)
                .id(UUID.randomUUID())
                .accountVerified(false)
                .build();
    }
    User dbUser(String email) {
        return User.builder()
                .email(email)
                .phoneNumber("08088492993")
                .status(Status.OTP_VALIDATION)
                .id(UUID.randomUUID())
                .accountVerified(false)
                .build();
    }
    User dbUser(String email, String phoneNumber) {
        return User.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .status(Status.OTP_VALIDATION)
                .id(UUID.randomUUID())
                .accountVerified(false)
                .build();
    }
    @Test
    void createUserShouldRaiseExceptionWhenUserPhoneNumberExists() throws DuplicateException {
        RegisterUserDto registerUserReq = registerReq();

        User user = dbUser("jerry@test.com", "08088492993");

        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(user));
//        when(emailService.sendMail(any())).thenReturn(RandomString.make(10));

        var exception = assertThrows(PhoneNumberAlreadyExistsException.class, () -> userService.createUser(registerUserReq));

        assertEquals(String.format("User with the Phone number: %s already exist", registerUserReq.getPhoneNumber()), exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void createUserShouldRaiseExceptionWhenUserExistByEmail() throws DuplicateException {
        RegisterUserDto registerUserReq = registerReq();

        User user = dbUser("test@test.com", "08135751087");

        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(user));

        var exception = assertThrows(DuplicateException.class, () -> userService.createUser(registerUserReq));

        assertEquals(String.format("User with the email: %s already exist", registerUserReq.getEmail()), exception.getMessage());
        verify(userRepository, never()).save(user);
    }


    @Test
    void createUserShouldReturnSuccess() {
        User newUser = dbUser();
        RegisterUserDto registerUserDto = registerReq();
        String messageId = RandomString.make(10);
        String otp = "123456";
        VerificationModel verificationModel = VerificationModel.builder()
                .userId(newUser.getId())
                .mode(OtpModeEnum.EMAIL)
                .type(VerificationType.REGISTER)
                .pin("123457")
                .messageId(messageId)
                .id(UUID.randomUUID())
                .build();
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(newUser.getEmail())
                .subject("Activate account")
                .recipientEmail(newUser.getEmail())
                .recipientName(newUser.getEmail())
                .htmlTemplate(Utility.getEmailTemplate(VerificationType.REGISTER, newUser.getEmail(), otp))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(emailService.sendMail(emailRequest)).thenReturn(messageId);
        when(emailService.saveVerification(OtpModeEnum.EMAIL, messageId, newUser.getId(), otp, VerificationType.REGISTER))
                .thenReturn(verificationModel);

        CreateUserRes response = userService.createUser(registerUserDto);
        assertNotNull(response.getUser());
        assertEquals(Status.OTP_VALIDATION, response.getUser().getStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUserShouldReturnSuccessForExistingUser(){

        User newUser = dbUser();
        RegisterUserDto registerUserDto = registerReq();
        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(newUser));
//        when(emailService.sendMail())

        CreateUserRes response = userService.createUser(registerUserDto);
        assertNotNull(response.getUser());
        assertEquals(Status.OTP_VALIDATION, response.getUser().getStatus());
        verify(userRepository,never()).save(any(User.class));
    }
}