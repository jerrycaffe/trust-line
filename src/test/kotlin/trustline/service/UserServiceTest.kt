package trustline.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import trustline.appuser.dto.*
import trustline.appuser.model.User
import trustline.notification.model.VerificationModel
import trustline.appuser.repository.UserRepository
import trustline.appuser.service.EmailServiceImpl
import trustline.appuser.service.UserServiceImpl
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.exception.PhoneNumberAlreadyExistsException
import trustline.config.security.CustomUserDetailsService
import trustline.config.security.JWTConfig
import java.time.LocalDateTime
import java.util.*

class UserServiceTest {
    val userRepository = mockk<UserRepository>()
    val userDetails = mockk<CustomUserDetailsService>()
    val jwtConfig = mockk<JWTConfig>()
    val authenticationManager = mockk<AuthenticationManager>()
    val passwordEncoder = BCryptPasswordEncoder()
    val emailService = mockk<EmailServiceImpl>()


    var userService = UserServiceImpl(
        userRepository, userDetails, jwtConfig, authenticationManager, passwordEncoder, emailService
    )

    private fun registerReq() = RegisterUserDto(
        email = "test@test.com",
        password = "test1234",
        phoneNumber = "08088492993"
    )

    private fun dbUser(
        email: String = "test@test.com",
        phoneNumber: String = "08088492993"
    ) = User(
        id = UUID.randomUUID(),
        email = email,
        phoneNumber = phoneNumber,
        status = Status.OTP_VALIDATION,
        isAccountVerified = false,
        authProvider = AuthProvider.LOCAL,
        password = ""
    )

    //
//    /* --------------------------------------------------------
//       CREATE USER TESTS
//    --------------------------------------------------------- */
//
    @Test
    fun `should throw exception when phone number or email already exists`() {
        val req = registerReq()
        val existingUser = dbUser(email = "jerry@test.com")

        every { userRepository.findByEmailOrPhoneNumber(any(), any()) } returns Optional.of(
            existingUser
        )

        val ex = assertThrows<PhoneNumberAlreadyExistsException> { userService.createUser(req) }

        assertEquals(
            "User with the phone number: ${req.phoneNumber} already exists",
            ex.message
        )

    }

    @Test
    fun `should successfully create new user`() {
        val user = dbUser()
        val req = registerReq()

        val verification = verificationModel(user)
        every { userRepository.save(any()) } returns user
        every { emailService.saveVerification(any(), any(), any(), any(), any()) } returns verification
        every { userRepository.findByEmailOrPhoneNumber(any(), any()) } returns Optional.empty()
        every { emailService.sendMail(any()) } returns UUID.randomUUID().toString()

        val response = userService.createUser(req)

        assertNotNull(response.user)
        assertEquals(Status.OTP_VALIDATION, response.user?.status)

        verify(atMost = 1) { userRepository.save(any()) }
    }

    //
    @Test
    fun `should reuse existing unverified user`() {
        val user = dbUser()
        val req = registerReq()
        val verification = verificationModel(user)

        every { userRepository.findByEmailOrPhoneNumber(any(), any()) } returns Optional.of(user)
        every { emailService.saveVerification(any(), any(), any(), any(), any()) } returns verification
        every { emailService.sendMail(any()) } returns UUID.randomUUID().toString()

        val response = userService.createUser(req)

        assertNotNull(response.user)
        assertEquals(Status.OTP_VALIDATION, response.user?.status)
    }
//
//    /* --------------------------------------------------------
//       VERIFY OTP TESTS
//    --------------------------------------------------------- */

    @Test
    fun `verifyOtp should fail when verification not found`() {
        val req = OtpRequest(
            userId = UUID.randomUUID(),
            verificationId = RandomString.make(6)
        )
        every { emailService.getbyUserIdAndPin(any(), any()) } returns Optional.empty()
        val ex = assertThrows<NotFoundException> { userService.verifyOtp(req) }

        assertEquals(
            "No Previous verification found",
            ex.message
        )

    }

    //
    @Test
    fun `verifyOtp should fail when token expired`() {
        val userId = UUID.randomUUID()
        val pin = RandomString.make(6)

        val verification = VerificationModel().apply {
            this.userId = userId
            this.type = VerificationType.REGISTER
            ReflectionTestUtils.setField(
                this,
                "createdAt",
                LocalDateTime.now().minusHours(3)
            )
        }

        every { emailService.getbyUserIdAndPin(any(), any()) } returns Optional.of(verification)

        val ex = assertThrows<BadRequestException> { userService.verifyOtp(OtpRequest(pin, userId)) }

        assertEquals(
            "Token expired, initiate another verification",
            ex.message
        )


    }

    //
    @Test
    fun `verifyOtp should fail when user not found`() {
        val userId = UUID.randomUUID()
        val pin = RandomString.make(6)

        val verification = VerificationModel().apply {
            this.userId = userId
            this.type = VerificationType.REGISTER
            ReflectionTestUtils.setField(this, "createdAt", LocalDateTime.now())
        }

        every {
            emailService.getbyUserIdAndPin(any(), any())
        } returns Optional.of(verification)

        every { userRepository.findById(any()) } returns Optional.empty()

        val ex = assertThrows<NotFoundException> { userService.verifyOtp(OtpRequest(pin, userId)) }

        assertEquals("User not found, verification cannot be completed", ex.message)
    }

    //
//    /* --------------------------------------------------------
//       HELPERS
//    --------------------------------------------------------- */
//
    private fun verificationModel(user: User): VerificationModel =
        VerificationModel().apply {
            userId = user.id
            pin = "123456"
            mode = OtpModeEnum.EMAIL
            type = VerificationType.REGISTER
            messageId = RandomString.make(10)
        }

    companion object {
        const val ACTIVATE_ACCOUNT = "Activate Trustline Account"
    }
}
