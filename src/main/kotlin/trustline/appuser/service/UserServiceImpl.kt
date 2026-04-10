package trustline.appuser.service;


import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import trustline.appuser.Utility
import trustline.appuser.dto.*
import trustline.appuser.model.UserModel
import trustline.notification.model.VerificationModel
import trustline.appuser.repository.UserRepository
import trustline.config.exception.*
import trustline.config.security.CustomUserDetailsService
import trustline.config.security.JWTConfig
import java.time.LocalDateTime
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtConfig: JWTConfig,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder(),
    private val emailService: EmailService
) : UserService {

    private val log = KotlinLogging.logger {}

    val ACTIVATE_ACCOUNT = "Activate Trustline Account"
    val RESET_PASSWORD = "Reset Password";
    val RESEND_OTP = "Trustline Resend OTP";
    val PREV_VERIFICATION_NOT_FOUND = "No Previous verification found";

    override fun createUser(user: RegisterUserDto): CreateUserRes? {
        val prevUser = userRepository.findByEmailOrPhoneNumber(user.email!!, user.password!!.trim())
        val otp: String = generateOtpPin();
        if (prevUser.isPresent) return handleUserExists(prevUser.get(), user, otp)

//      TODO send OTP to user
//        val newUser: UserModel = newUser(user)
//        val savedUser: UserModel = userRepository.save(newUser)
//
//        val emailVerification: VerificationModel? =
//            generateOtp(savedUser, otp, ACTIVATE_ACCOUNT, VerificationType.REGISTER)
//
//        return buildUserResponse(savedUser, emailVerification!!);
        return null
    }

    fun generateOtpPin(): String {
        return String.format("%06d", Utility.generateSixDigitsNumber());
    }

    fun generateOtp(
        user: UserModel,
        otp: String,
        subject: String,
        verificationType: VerificationType
    ): VerificationModel? {

        val emailRequest = EmailRequest(
            user.email,
            user.email,
            user.id,
            Utility.getEmailTemplate(
                verificationType,
                user.email,
                otp
            ),
            subject,
        )

        val messageId = emailService.sendMail(emailRequest)

        return emailService.saveVerification(
            OtpModeEnum.EMAIL,
            messageId!!,
            user.id!!,
            otp,
            verificationType
        )
    }


    fun handleUserExists(existingUser: UserModel, registerUserDto: RegisterUserDto, otp: String): CreateUserRes {
        val emailMatches: Boolean = existingUser.email == registerUserDto.email
        val phoneMatches = existingUser.phoneNumber == registerUserDto.phoneNumber

        if (phoneMatches && !emailMatches)
            throw PhoneNumberAlreadyExistsException(registerUserDto.phoneNumber!!);
        if (emailMatches && !phoneMatches) throw EmailAlreadyExistsException(registerUserDto.email!!);
        if (existingUser.status == Status.OTP_VALIDATION) {
            val emailVerification: VerificationModel? =
                generateOtp(existingUser, otp, ACTIVATE_ACCOUNT, VerificationType.REGISTER);
            return buildUserResponse(existingUser, emailVerification!!);
        }
        throw PhoneNumberAndEmailAlreadyExistsException(existingUser.phoneNumber!!, existingUser.email);

    }

    fun buildUserResponse(user: UserModel, verification: VerificationModel): CreateUserRes {
        return CreateUserRes(user, verification.id)
    }


//    fun newUser(user: RegisterUserDto): UserModel {
//        return UserModel(
//            email = user.email!!,
//            authProvider = AuthProvider.LOCAL,
//            password = passwordEncoder.encode(user.password),
//            isDeleted = false,
//            status = Status.OTP_VALIDATION,
//            phoneNumber = user.phoneNumber
//        )
//    }


    override fun login(loginReq: LoginReq): LoginRes<UserResponseDto> {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginReq.userName, loginReq.password
            )

        );
        val user: UserModel =
            userRepository.findByEmail(loginReq.userName!!).orElseThrow { UsernameNotFoundException(loginReq.userName) }
        val loginUser = LoginRes(
            data = UserResponseDto.fromUser(user),
            message = "Login Successful",
            accessToken = if (user.status == Status.VERIFIED) jwtConfig.generateToken(user) else null
        )
        return loginUser;
    }

    override fun verifyOtp(otpRequest: OtpRequest): OtpVerificationResponse {
        log.info{"otp request received with details $otpRequest"}
        val verifyUser: VerificationModel =
            emailService.getbyUserIdAndPin(otpRequest.userId!!, otpRequest.verificationId!!)
                .orElseThrow { NotFoundException(PREV_VERIFICATION_NOT_FOUND) }
        if (verifyUser.createdAt!!.isBefore(LocalDateTime.now().minusHours(2)))
            throw BadRequestException("Token expired, initiate another verification");
//        Update user status if it otp is for user verification
        if (verifyUser.type!! == VerificationType.REGISTER) {
            verifyUserRegistration(otpRequest.userId)
        }
        return OtpVerificationResponse("Verification Successful", verifyUser.id);
    }


    fun verifyUserRegistration(userId: UUID) {
        val userDetails = userRepository.findById(userId)
            .orElseThrow { NotFoundException("User not found, verification cannot be completed") }

        userDetails.isAccountVerified = true
        userDetails.status = Status.VERIFIED

        userRepository.save(userDetails)
    }

    override fun forgotPassword(forgotPasswordReq: ForgotPasswordReq): ForgotPasswordRes {
        val user = userRepository
            .findByEmail(forgotPasswordReq.email!!)
            .orElseThrow {
                BadRequestException("You cannot perform this action as user does not exist")
            }

        val generatedOtp = generateOtp(
            user,
            generateOtpPin(),
            RESET_PASSWORD,
            VerificationType.RESET_PASSWORD
        )

        return ForgotPasswordRes(generatedOtp?.id)
    }

    override fun resetPassword(resetPasswordReq: ResetPasswordReq): UserModel {

        val user = userRepository.findByEmail(resetPasswordReq.userName!!)
            .orElseThrow {
                EmailNotFoundException(resetPasswordReq.userName)
            }

        val newPassword = passwordEncoder.encode(resetPasswordReq.newPassword)
        user.password = newPassword

        return userRepository.save(user)
    }


    override fun resendOtp(resendOtpRequest: ResendOtpRequest): OtpVerificationResponse {

        val previousVerification = emailService
            .getVerificationById(resendOtpRequest.prevOtpId!!)
            .orElseThrow { NotFoundException(PREV_VERIFICATION_NOT_FOUND) }

        val user = userRepository.findById(previousVerification.userId!!)
            .orElseThrow { NotFoundException("User not found") }

        val newOtp = generateOtp(
            user,
            generateOtpPin(),
            RESEND_OTP,
            previousVerification.type!!
        )

        return OtpVerificationResponse(
            "Resend OTP Successful",
            newOtp?.id
        )
    }


}
