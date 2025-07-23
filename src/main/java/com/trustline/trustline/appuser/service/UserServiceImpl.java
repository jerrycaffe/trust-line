package com.trustline.trustline.appuser.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.trustline.trustline.appuser.dto.*;
import com.trustline.trustline.appuser.model.AuthProvider;
import com.trustline.trustline.appuser.model.Status;
import com.trustline.trustline.appuser.model.User;
import com.trustline.trustline.appuser.model.VerificationModel;
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
    public User createUser(RegisterUserDto user) {
        Optional<User> prevUser = userRepository.findByEmailOrPhoneNumber(user.getEmail(), user.getPhoneNumber());
        if (prevUser.isPresent()) return handleUserExists(prevUser.get(), user);

//      TODO send OTP to user
        User newUser = newUser(user);
        User savedUser = userRepository.save(newUser);
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(user.getEmail())
                .recipientName(newUser.getEmail())
                .subject("Activate Trustline Account")
                .recipientId(savedUser.getId())
                .build();

//        TODO Generate Token to be sent to the phone number
        emailService.sendMail(emailRequest);
//        TODO Ensure user verifies account
        return savedUser;
    }

    private User handleUserExists(User existingUser, RegisterUserDto registerUserDto) {
        boolean emailMatches = existingUser.getEmail().equals(registerUserDto.getEmail());
        boolean phoneMatches = existingUser.getPhoneNumber().equals(registerUserDto.getPhoneNumber());

        if (phoneMatches && !emailMatches)
            throw new PhoneNumberAlreadyExistsException(registerUserDto.getPhoneNumber());
        else if (emailMatches && !phoneMatches) throw new EmailAlreadyExistsException(registerUserDto.getEmail());
        else if (existingUser.getStatus() == Status.OTP_VALIDATION) return existingUser;
        throw new PhoneNumberAndEmailAlreadyExistsException(existingUser.getPhoneNumber(), existingUser.getEmail());

    }

    private User newUser(RegisterUserDto user) {
        return User.builder()
                .email(user.getEmail())
                .authProvider(AuthProvider.LOCAL)
                .gender(user.getGender())
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
    public String verifyOtp(OtpRequest otpRequest) {
        Boolean userVerified = emailService.verifyOtp(otpRequest.getUserId(), otpRequest.getVerificationId());
        if (!userVerified) throw new BadRequestException("Verification failed");
        User userDetails = userRepository.findById(otpRequest.getUserId()).get();
        userDetails.setAccountVerified(true);
        userDetails.setStatus(Status.VERIFIED);
        userRepository.save(userDetails);
        return "Verification Successful!";

    }

    @Override
    public User forgotPassword(ForgotPasswordReq forgotPasswordReq) {
        String phoneNumber = forgotPasswordReq.getPhoneNumber();
        String email = forgotPasswordReq.getEmail();
        if (phoneNumber == null && email == null)
            throw new BadRequestException("Email and phone number cannot be empty");
        return userRepository.findByEmailOrPhoneNumber(email, phoneNumber).orElseThrow(() -> new BadRequestException("User not found"));


    }

    @Override
    public User resetPassword(ResetPasswordReq resetPasswordReq) {
        User user = userRepository.findByEmail(resetPasswordReq.getUserName())
                .orElseThrow(() -> new EmailNotFoundException(resetPasswordReq.getUserName()));
        String newPassword = passwordEncoder.encode(resetPasswordReq.getNewPassword());
        user.setPassword(newPassword);
        return userRepository.save(user);
    }


}
