package com.trustline.trustline.appuser.service;


import com.trustline.trustline.appuser.Utility;
import com.trustline.trustline.appuser.dto.*;
import com.trustline.trustline.appuser.model.*;
import com.trustline.trustline.appuser.repository.UserRepository;
import com.trustline.trustline.config.exception.*;

import com.trustline.trustline.config.security.CustomUserDetailsService;
import com.trustline.trustline.config.security.JWTConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JWTConfig jwtConfig;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;

    @Override
    public CreateUserRes createUser(RegisterUserDto user) {
        Optional<User> prevUser = userRepository.findByEmailOrPhoneNumber(user.getEmail(), user.getPhoneNumber());
        String otp = String.valueOf(Utility.generateSixDigitsNumber());
        if (prevUser.isPresent()) return handleUserExists(prevUser.get(), user, otp);

//      TODO send OTP to user
        User newUser = newUser(user);
        User savedUser = userRepository.save(newUser);

        VerificationModel emailVerification = generateOtp(savedUser, otp);

        return CreateUserRes.builder()
                .user(savedUser)
                .otpId(emailVerification.getId())
                .build();
    }

    private VerificationModel generateOtp(User user, String otp) {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(user.getEmail())
                .recipientName(user.getEmail())
                .subject("Activate Trustline Account")
                .htmlTemplate(Utility.welcomeEmailTemplate(user.getEmail(), otp))
                .recipientId(user.getId())
                .build();

        //        TODO Generate Token to be sent to the phone number
        String messageId = emailService.sendMail(emailRequest);
        return emailService.saveVerification(OtpModeEnum.EMAIL, messageId, user.getId(), otp, VerificationType.REGISTER);
    }

    private CreateUserRes handleUserExists(User existingUser, RegisterUserDto registerUserDto, String otp) {
        boolean emailMatches = existingUser.getEmail().equals(registerUserDto.getEmail());
        boolean phoneMatches = existingUser.getPhoneNumber().equals(registerUserDto.getPhoneNumber());

        if (phoneMatches && !emailMatches)
            throw new PhoneNumberAlreadyExistsException(registerUserDto.getPhoneNumber());
        else if (emailMatches && !phoneMatches) throw new EmailAlreadyExistsException(registerUserDto.getEmail());
        else if (existingUser.getStatus() == Status.OTP_VALIDATION) {
            VerificationModel emailVerification = generateOtp(existingUser, otp);
            return CreateUserRes.builder()
                    .otpId(emailVerification.getId())
                    .user(existingUser)
                    .build();
        }
        throw new PhoneNumberAndEmailAlreadyExistsException(existingUser.getPhoneNumber(), existingUser.getEmail());

    }

    private User newUser(RegisterUserDto user) {
        return User.builder()
                .email(user.getEmail())
                .authProvider(AuthProvider.LOCAL)
                .password(passwordEncoder.encode(user.getPassword().trim()))
                .accountVerified(false)
                .status(Status.OTP_VALIDATION)
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    public LoginRes<UserResponseDto> login(LoginReq loginReq) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginReq.getUserName(), loginReq.getPassword()));

        User user = userRepository.findByEmail(loginReq.getUserName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        LoginRes<UserResponseDto> loginUser = new LoginRes<>();
        String token = jwtConfig.generateToken(user);
        loginUser.setData(UserResponseDto.fromUser(user));
        loginUser.setMessage("Login Successful");
        if (user.getStatus() == Status.OTP_VALIDATION) return loginUser;
        loginUser.setAccessToken(token);

        return loginUser;
    }

    @Override
    public OtpVerificationResponse verifyOtp(OtpRequest otpRequest) {
        log.info("otp request received with details {}", otpRequest);
        VerificationModel userVerified = emailService.verifyOtp(otpRequest.getUserId(), otpRequest.getVerificationId());
//        Update user status if it otp is for user verification
        if (userVerified.getType().equals(VerificationType.REGISTER)) verifyUserRegistration(otpRequest.getUserId());
        return new OtpVerificationResponse("Verification Successful");
    }

    private void verifyUserRegistration(UUID userId) {
        User userDetails = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found, verification cannot be completed"));
        userDetails.setAccountVerified(true);
        userDetails.setStatus(Status.VERIFIED);
        userRepository.save(userDetails);
    }

    @Override
    public User forgotPassword(ForgotPasswordReq forgotPasswordReq) {
        String email = forgotPasswordReq.getEmail();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadRequestException("You cannot perform this action as user does not exist"));

        String resetOtp = String.valueOf(Utility.generateSixDigitsNumber());

        EmailRequest emailRequest = EmailRequest.builder()
                .recipientId(user.getId())
                .subject("Reset Password")
                .recipientEmail(email)
                .htmlTemplate(Utility.forgotPasswordEmailTemplate(user.getEmail(), resetOtp))
                .recipientName(email)
                .build();

        String messageId = emailService.sendMail(emailRequest);
        emailService.saveVerification(OtpModeEnum.EMAIL, messageId, user.getId(), resetOtp, VerificationType.RESET_PASSWORD);
        return user;
    }

    @Override
    public User resetPassword(ResetPasswordReq resetPasswordReq) {
        User user = userRepository.findByEmail(resetPasswordReq.getUserName())
                .orElseThrow(() -> new EmailNotFoundException(resetPasswordReq.getUserName()));
        String newPassword = passwordEncoder.encode(resetPasswordReq.getNewPassword());
        user.setPassword(newPassword);
        return userRepository.save(user);
    }

    @Override
    public OtpVerificationResponse resendOtp(ResendOtpRequest resendOtpRequest) {
        VerificationModel prevVerification = emailService.getVerificationById(resendOtpRequest.getPrevOtpId());
//        TODO: Use prev data to generate new verification and return response
        return null;
    }


}
