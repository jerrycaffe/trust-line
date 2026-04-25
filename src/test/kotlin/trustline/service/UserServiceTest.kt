package trustline.service

import com.cloudinary.Cloudinary
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import trustline.appuser.dto.*
import trustline.appuser.model.RoleModel
import trustline.appuser.model.UserModel
import trustline.appuser.repository.PermissionRepository
import trustline.appuser.repository.RolesRepository
import trustline.appuser.repository.UserRepository
import trustline.appuser.service.EmailService
import trustline.appuser.service.UserServiceImpl
import trustline.config.exception.DuplicateException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.model.InstitutionModel
import trustline.institution.service.InstitutionService
import trustline.notification.model.VerificationModel
import java.util.*

class UserServiceTest {

    // ── collaborators ────────────────────────────────────────────────────────
    private val userRepository = mockk<UserRepository>()
    private val rolesRepository = mockk<RolesRepository>()
    private val permissionRepository = mockk<PermissionRepository>()
    private val jwtConfig = mockk<JWTConfigService>()
    private val authenticationManager = mockk<AuthenticationManager>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val emailService = mockk<EmailService>()
    private val institutionService = mockk<InstitutionService>()
    private val cloudinary = mockk<Cloudinary>()

    private lateinit var userService: UserServiceImpl

    // ── fixtures ─────────────────────────────────────────────────────────────
    private val institutionId: UUID = UUID.randomUUID()
    private val institution = InstitutionModel(id = institutionId, name = "TEST", phoneNumber = "0800000000000")
    private val userRole = RoleModel(id = UUID.randomUUID(), name = "User")

    @BeforeEach
    fun setUp() {
        userService = UserServiceImpl(
            userRepository, rolesRepository, permissionRepository,
            jwtConfig, authenticationManager, passwordEncoder,
            emailService, institutionService, cloudinary
        )
    }

    private fun registerReq() = RegisterUserDto(
        email = "test@test.com",
        password = "test1234",
        phoneNumber = "08088492993",
        institutionId = institutionId
    )

    private fun dbUser(
        email: String = "test@test.com",
        phoneNumber: String = "08088492993"
    ) = UserModel(
        id = UUID.randomUUID(),
        email = email,
        phoneNumber = phoneNumber,
        status = Status.OTP_VALIDATION,
        isAccountVerified = false,
        authProvider = AuthProvider.LOCAL,
        password = "",
        institution = institution
    )

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    fun `createUser should throw DuplicateException when email already exists`() {
        val req = registerReq()
        val existingUser = dbUser(email = req.email!!)

        every { institutionService.getInstitutionById(institutionId) } returns institution
        every { userRepository.findByEmailOrPhoneNumberAndInstitutionId(req.email!!, req.phoneNumber!!, institutionId) } returns existingUser
        every { rolesRepository.findByNameAndInstitutionIdIsNull("User") } returns userRole

        assertThrows<DuplicateException> { userService.createUser(req) }
    }

    @Test
    fun `createUser should throw NotFoundException when institution not found`() {
        val req = registerReq()

        every { institutionService.getInstitutionById(institutionId) } returns null

        assertThrows<NotFoundException> { userService.createUser(req) }
    }

    @Test
    fun `createUser should succeed when all details are correct`() {
        val req = registerReq()
        val savedUser = dbUser()

        every { institutionService.getInstitutionById(institutionId) } returns institution
        every { userRepository.findByEmailOrPhoneNumberAndInstitutionId(req.email!!, req.phoneNumber!!, institutionId) } returns null
        every { rolesRepository.findByNameAndInstitutionIdIsNull("User") } returns userRole
        every { userRepository.save(any()) } returns savedUser
        every { emailService.sendMail(any()) } returns UUID.randomUUID().toString()
        every { emailService.saveVerification(any(), any(), any(), any(), any(), any()) } returns mockk(relaxed = true)

        val response = userService.createUser(req)

        assertNotNull(response.email)
        assertEquals(Status.OTP_VALIDATION, response.status)
        verify(atMost = 1) { userRepository.save(any()) }
    }

    // ── verifyOtp ─────────────────────────────────────────────────────────────

    @Test
    fun `verifyOtp should throw NotFoundException when verification not found`() {
        every { emailService.getbyUserIdAndPinAndStatus(any(), any(), any()) } returns null

        assertThrows<NotFoundException> {
            userService.verifyOtp(OtpRequest(verificationId = "123456", userId = UUID.randomUUID()))
        }
    }

    @Test
    fun `verifyOtp should succeed and return verification ID`() {
        val userId = UUID.randomUUID()
        val verificationId = UUID.randomUUID()
        val user = dbUser()
        val verification = VerificationModel(
            id = verificationId,
            messageId = "msgId",
            user = user,
            pin = "123456",
            mode = OtpModeEnum.EMAIL,
            type = VerificationType.REGISTER,
            status = Status.UNVERIFIED
        )
        val verifiedUser = user.copy(isAccountVerified = true, status = Status.VERIFIED)

        every { emailService.getbyUserIdAndPinAndStatus(userId, "123456", Status.UNVERIFIED) } returns verification
        every { userRepository.findById(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } returns verifiedUser
        every { emailService.updateVerification(any()) } returns verification

        val result = userService.verifyOtp(OtpRequest(verificationId = "123456", userId = userId))

        assertEquals(verificationId, result.otpId)
    }
}
