package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.CreateUserRes;
import com.trustline.trustline.appuser.dto.OtpRequest;
import com.trustline.trustline.appuser.dto.RegisterUserDto;
import com.trustline.trustline.appuser.model.*;
import com.trustline.trustline.appuser.repository.UserRepository;
import com.trustline.trustline.config.exception.*;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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

    public static final String ACTIVATE_ACCOUNT = "Activate Trustline Account";

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
    void createUserShouldRaiseExceptionWhenUserPhoneNumberExists() throws PhoneNumberAlreadyExistsException {
        RegisterUserDto registerUserReq = registerReq();

        User user = dbUser("jerry@test.com", "08088492993");

        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(user));
//        when(emailService.sendMail(any())).thenReturn(RandomString.make(10));

        var exception = assertThrows(PhoneNumberAlreadyExistsException.class, () -> userService.createUser(registerUserReq));

        assertEquals(String.format("User with the Phone number: %s already exist", registerUserReq.getPhoneNumber()), exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void createUserShouldRaiseExceptionWhenUserExistByEmail() throws EmailAlreadyExistsException {
        RegisterUserDto registerUserReq = registerReq();

        User user = dbUser("test@test.com", "08135751087");

        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(user));

        var exception = assertThrows(DuplicateException.class, () -> userService.createUser(registerUserReq));

        assertEquals(String.format("User with the email: %s already exist", registerUserReq.getEmail()), exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void createUserShouldRaiseExceptionWhenUserExistByEmailAndPhone() throws PhoneNumberAndEmailAlreadyExistsException {
        RegisterUserDto registerUserReq = registerReq();

        User existingUser = dbUser();
        existingUser.setStatus(Status.VERIFIED);

        when(userRepository.findByEmailOrPhoneNumber(registerUserReq.getEmail(), registerUserReq.getPhoneNumber()))
                .thenReturn(Optional.of(existingUser));

        var exception = assertThrows(PhoneNumberAndEmailAlreadyExistsException.class, () -> userService.createUser(registerUserReq));

        assertEquals(String.format("User with the Phone number: %s and Email: %s already exist", registerUserReq.getPhoneNumber(), registerUserReq.getEmail()), exception.getMessage());
        verify(userRepository, never()).save(existingUser);
    }

    private VerificationModel getVerificationModelt(User user, String messageId) {
        return VerificationModel.builder()
                .userId(user.getId())
                .mode(OtpModeEnum.EMAIL)
                .type(VerificationType.REGISTER)
                .pin("123457")
                .messageId(messageId)
                .id(UUID.randomUUID())
                .build();
    }

    @Test
    void createUserShouldReturnSuccess() {
        User newUser = dbUser();
        RegisterUserDto registerUserDto = registerReq();
        String messageId = RandomString.make(10);
        VerificationModel verificationModel = getVerificationModelt(newUser, messageId);


        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(emailService.saveVerification(any(), any(), any(), any(), any())).thenReturn(verificationModel);

        CreateUserRes response = userService.createUser(registerUserDto);
        assertNotNull(response.getUser());
        assertEquals(Status.OTP_VALIDATION, response.getUser().getStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUserShouldReturnSuccessForExistingUser() {

        User newUser = dbUser();
        RegisterUserDto registerUserDto = registerReq();
        VerificationModel verificationModel = getVerificationModelt(newUser, RandomString.make(10));

        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(newUser));
        when(emailService.saveVerification(any(), any(), any(), any(), any())).thenReturn(verificationModel);


        CreateUserRes response = userService.createUser(registerUserDto);
        assertNotNull(response.getUser());
        assertEquals(Status.OTP_VALIDATION, response.getUser().getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login() {
    }

    @Test
    void verifyOtp() {
//        TODO: Get verificationid from user and userid
//        throw exception once user cannot be found
//        throw exception when user is found and no verification was strted
//        once user is found and verification model is located, change status
    }

    @Test
    void verifyOtpShouldRaiseExceptionWhenVerificationNotFound() throws NotFoundException{
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUserId(UUID.randomUUID());
        otpRequest.setVerificationId(RandomString.make(6));

        var exception = assertThrows(NotFoundException.class, () -> userService.verifyOtp(otpRequest));
        assertEquals("No Previous verification found", exception.getMessage());
    }
    @Test
    void verifyOtpShouldRaiseExceptionWhenTokenExpires() throws BadRequestException{

        UUID userId = UUID.randomUUID();
        String pin = RandomString.make(6);

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUserId(userId);
        otpRequest.setVerificationId(pin);

        VerificationModel verificationModel = new VerificationModel();
        verificationModel.setUserId(userId);
        verificationModel.setType(VerificationType.REGISTER);
        ReflectionTestUtils.setField(verificationModel, "createdAt", LocalDateTime.now().minusHours(3));

        when(emailService.getbyUserIdAndPin(any(), anyString())).thenReturn(Optional.of(verificationModel));

        var exception = assertThrows(BadRequestException.class, () -> userService.verifyOtp(otpRequest));
        assertEquals("Token expired, initiate another verification", exception.getMessage());
    }

    @Test
    void verifyOtpShouldRaiseExceptionWhenUserIsNotFound() throws NotFoundException{

        UUID userId = UUID.randomUUID();
        String pin = RandomString.make(6);

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUserId(userId);
        otpRequest.setVerificationId(pin);

        VerificationModel verificationModel = new VerificationModel();
        verificationModel.setUserId(userId);
        verificationModel.setType(VerificationType.REGISTER);
        ReflectionTestUtils.setField(verificationModel, "createdAt", LocalDateTime.now());

        when(emailService.getbyUserIdAndPin(any(), anyString())).thenReturn(Optional.of(verificationModel));

        var exception = assertThrows(NotFoundException.class, () -> userService.verifyOtp(otpRequest));
        assertEquals("User not found, verification cannot be completed", exception.getMessage());
    }

    @Test
    void verifyOtpShouldBeSuccessful() throws NotFoundException{

        UUID userId = UUID.randomUUID();
        String pin = RandomString.make(6);



        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUserId(userId);
        otpRequest.setVerificationId(pin);

        VerificationModel verificationModel = new VerificationModel();
        verificationModel.setUserId(userId);
        verificationModel.setType(VerificationType.REGISTER);
        ReflectionTestUtils.setField(verificationModel, "createdAt", LocalDateTime.now());

        when(emailService.getbyUserIdAndPin(any(), anyString())).thenReturn(Optional.of(verificationModel));
        when(userRepository.findById(any()))


        var exception = assertThrows(NotFoundException.class, () -> userService.verifyOtp(otpRequest));
        assertEquals("User not found, verification cannot be completed", exception.getMessage());
    }



    @Test
    void verifyUserRegistration() {
    }

    @Test
    void forgotPassword() {
    }

    @Test
    void resetPassword() {
    }

    @Test
    void resendOtp() {
    }
}