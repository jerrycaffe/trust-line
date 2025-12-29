package trustline.appuser.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trustline.appuser.Utility;
import trustline.appuser.dto.*;
import trustline.appuser.model.*;
import trustline.appuser.repository.UserRepository;
import trustline.config.exception.*;
import trustline.config.security.CustomUserDetailsService;
import trustline.config.security.JWTConfig;

import java.time.LocalDateTime;
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
    public static final String ACTIVATE_ACCOUNT = "Activate Trustline Account";
    public static final String RESET_PASSWORD = "Reset Password";
    public static final String RESEND_OTP = "Trustline Resend OTP";
    public static final String PREV_VERIFICATION_NOT_FOUND = "No Previous verification found";

    @Override
    public CreateUserRes createUser(RegisterUserDto user) {
        Optional<User> prevUser = userRepository.findByEmailOrPhoneNumber(user.getEmail(), user.getPhoneNumber());
        String otp = generateOtpPin();
        if (prevUser.isPresent()) return handleUserExists(prevUser.get(), user, otp);

//      TODO send OTP to user
        User newUser = newUser(user);
        User savedUser = userRepository.save(newUser);

        VerificationModel emailVerification = generateOtp(savedUser, otp, ACTIVATE_ACCOUNT, VerificationType.REGISTER);

        return buildUserResponse(savedUser, emailVerification);
    }

    private String generateOtpPin() {
        return String.format("%06d", Utility.generateSixDigitsNumber());
    }

    private VerificationModel generateOtp(User user, String otp, String subject, VerificationType verificationType) {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(user.getEmail())
                .recipientName(user.getEmail())
                .subject(subject)
                .htmlTemplate(Utility.getEmailTemplate(verificationType, user.getEmail(), otp))
                .recipientId(user.getId())
                .build();
        String messageId = emailService.sendMail(emailRequest);
        return emailService.saveVerification(OtpModeEnum.EMAIL, messageId, user.getId(), otp, verificationType);
    }

    private CreateUserRes handleUserExists(User existingUser, RegisterUserDto registerUserDto, String otp) {
        boolean emailMatches = existingUser.getEmail().equals(registerUserDto.getEmail());
        boolean phoneMatches = existingUser.getPhoneNumber().equals(registerUserDto.getPhoneNumber());

        if (phoneMatches && !emailMatches)
            throw new PhoneNumberAlreadyExistsException(registerUserDto.getPhoneNumber());
        if (emailMatches && !phoneMatches) throw new EmailAlreadyExistsException(registerUserDto.getEmail());
        if (existingUser.getStatus() == Status.OTP_VALIDATION) {
            VerificationModel emailVerification = generateOtp(existingUser, otp, ACTIVATE_ACCOUNT, VerificationType.REGISTER);
            return buildUserResponse(existingUser, emailVerification);
        }
        throw new PhoneNumberAndEmailAlreadyExistsException(existingUser.getPhoneNumber(), existingUser.getEmail());

    }

    private CreateUserRes buildUserResponse(User user, VerificationModel verification) {
        return CreateUserRes.builder()
                .user(user)
                .otpId(verification.getId())
                .build();
    }


    private User newUser(RegisterUserDto user) {
        return User.builder()
                .email(user.getEmail())
                .authProvider(AuthProvider.LOCAL)
                .password(passwordEncoder.encode(user.getPassword()))
                .accountVerified(false)
                .status(Status.OTP_VALIDATION)
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    public LoginRes<UserResponseDto> login(LoginReq loginReq) {
       authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginReq.getUserName(), loginReq.getPassword()));

        User user = userRepository.findByEmail(loginReq.getUserName()).orElseThrow(()-> new UsernameNotFoundException(loginReq.getUserName()));
        LoginRes<UserResponseDto> loginUser = new LoginRes<>();
        String token = jwtConfig.generateToken(user);
        loginUser.setData(UserResponseDto.fromUser(user));
        loginUser.setMessage("Login Successful");
        if (user.getStatus() == Status.VERIFIED) loginUser.setAccessToken(token);
        return loginUser;
    }

    @Override
    public OtpVerificationResponse verifyOtp(OtpRequest otpRequest) {
        log.info("otp request received with details {}", otpRequest);
        VerificationModel verifyUser = emailService.getbyUserIdAndPin(otpRequest.getUserId(), otpRequest.getVerificationId()).orElseThrow(()-> new NotFoundException(PREV_VERIFICATION_NOT_FOUND));
        if (verifyUser.getCreatedAt().isBefore(LocalDateTime.now().minusHours(2)))
            throw new BadRequestException("Token expired, initiate another verification");
//        Update user status if it otp is for user verification
        if (verifyUser.getType().equals(VerificationType.REGISTER)) verifyUserRegistration(otpRequest.getUserId());
        return new OtpVerificationResponse("Verification Successful", verifyUser.getId());
    }


    public void verifyUserRegistration(UUID userId) {
        User userDetails = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found, verification cannot be completed"));
        userDetails.setAccountVerified(true);
        userDetails.setStatus(Status.VERIFIED);
        userRepository.save(userDetails);
    }

    @Override
    public ForgotPasswordRes forgotPassword(ForgotPasswordReq forgotPasswordReq) {

        User user = userRepository
                .findByEmail(forgotPasswordReq.getEmail())
                .orElseThrow(() -> new BadRequestException("You cannot perform this action as user does not exist"));

        VerificationModel generateOtp = generateOtp(user, generateOtpPin(), RESET_PASSWORD, VerificationType.RESET_PASSWORD);
        return new ForgotPasswordRes(generateOtp.getId());
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
        VerificationModel previousVerification = emailService.getVerificationById(resendOtpRequest.getPrevOtpId()).orElseThrow(()-> new NotFoundException(PREV_VERIFICATION_NOT_FOUND));
        User user = userRepository.findById(previousVerification.getUserId()).orElseThrow(() -> new NotFoundException("User not found"));

        VerificationModel newOtp = generateOtp(user, generateOtpPin(), RESEND_OTP, previousVerification.getType());
        return new OtpVerificationResponse("Resend OTP Successful", newOtp.getId());
    }


}
