package trustline.service

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserServiceTest {

//    @InjectMocks
//    lateinit var userService: UserServiceImpl
//
//    @Mock
//    lateinit var userRepository: UserRepository
//
//    @Mock
//    lateinit var emailService: EmailService
//
//    private fun registerReq() = RegisterUserDto(
//        email = "test@test.com",
//        password = "test1234",
//        phoneNumber = "08088492993"
//    )
//
//    private fun dbUser(
//        email: String = "test@test.com",
//        phoneNumber: String = "08088492993"
//    ) = User(
//        id = UUID.randomUUID(),
//        email = email,
//        phoneNumber = phoneNumber,
//        status = Status.OTP_VALIDATION,
//        isAccountVerified = false,
//        authProvider = AuthProvider.LOCAL,
//        password = ""
//    )
//
//    /* --------------------------------------------------------
//       CREATE USER TESTS
//    --------------------------------------------------------- */
//
//    @Test
//    fun `should throw exception when phone number already exists`() {
//        val req = registerReq()
//        val existingUser = dbUser(email = "jerry@test.com")
//
//        `when`(
//            userRepository.findByEmailOrPhoneNumber(req.email!!, req.phoneNumber!!)
//        ).thenReturn(Optional.of(existingUser))
//
//        val ex = assertThrows(PhoneNumberAlreadyExistsException::class.java) {
//            userService.createUser(req)
//        }
//
//        assertEquals(
//            "User with the Phone number: ${req.phoneNumber} already exist",
//            ex.message
//        )
//
//        verify(userRepository, never()).save(any())
//    }
//
//    @Test
//    fun `should throw exception when email already exists`() {
//        val req = registerReq()
//        val existingUser = dbUser(phoneNumber = "08135751087")
//
//        `when`(
//            userRepository.findByEmailOrPhoneNumber(req.email!!, req.phoneNumber!!)
//        ).thenReturn(Optional.of(existingUser))
//
//        val ex = assertThrows(EmailAlreadyExistsException::class.java) {
//            userService.createUser(req)
//        }
//
//        assertEquals(
//            "User with the email: ${req.email} already exist",
//            ex.message
//        )
//
//        verify(userRepository, never()).save(any())
//    }
//
//    @Test
//    fun `should throw exception when email and phone already exist`() {
//        val req = registerReq()
//        val existingUser = dbUser().apply {
//            status = Status.VERIFIED
//        }
//
//        `when`(
//            userRepository.findByEmailOrPhoneNumber(req.email!!, req.phoneNumber!!)
//        ).thenReturn(Optional.of(existingUser))
//
//        val ex = assertThrows(PhoneNumberAndEmailAlreadyExistsException::class.java) {
//            userService.createUser(req)
//        }
//
//        assertEquals(
//            "User with the Phone number: ${req.phoneNumber} and Email: ${req.email} already exist",
//            ex.message
//        )
//
//        verify(userRepository, never()).save(any())
//    }
//
//    @Test
//    fun `should successfully create new user`() {
//        val user = dbUser()
//        val req = registerReq()
//
//        val verification = verificationModel(user)
//
//        `when`(userRepository.save(any(User::class.java))).thenReturn(user)
//        `when`(
//            emailService.saveVerification(any(), any(), any(), any(), any())
//        ).thenReturn(verification)
//
//        val response = userService.createUser(req)
//
//        assertNotNull(response.user)
//        assertEquals(Status.OTP_VALIDATION, response.user?.status)
//
//        verify(userRepository, times(1)).save(any())
//    }
//
//    @Test
//    fun `should reuse existing unverified user`() {
//        val user = dbUser()
//        val req = registerReq()
//        val verification = verificationModel(user)
//
//        `when`(
//            userRepository.findByEmailOrPhoneNumber(req.email!!, req.phoneNumber!!)
//        ).thenReturn(Optional.of(user))
//
//        `when`(
//            emailService.saveVerification(any(), any(), any(), any(), any())
//        ).thenReturn(verification)
//
//        val response = userService.createUser(req)
//
//        assertNotNull(response.user)
//        assertEquals(Status.OTP_VALIDATION, response.user?.status)
//
//        verify(userRepository, never()).save(any())
//    }
//
//    /* --------------------------------------------------------
//       VERIFY OTP TESTS
//    --------------------------------------------------------- */
//
//    @Test
//    fun `verifyOtp should fail when verification not found`() {
//        val req = OtpRequest(
//            userId = UUID.randomUUID(),
//            verificationId = RandomString.make(6)
//        )
//
//        val ex = assertThrows(NotFoundException::class.java) {
//            userService.verifyOtp(req)
//        }
//
//        assertEquals("No Previous verification found", ex.message)
//    }
//
//    @Test
//    fun `verifyOtp should fail when token expired`() {
//        val userId = UUID.randomUUID()
//        val pin = RandomString.make(6)
//
//        val verification = VerificationModel().apply {
//            this.userId = userId
//            this.type = VerificationType.REGISTER
//            ReflectionTestUtils.setField(
//                this,
//                "createdAt",
//                LocalDateTime.now().minusHours(3)
//            )
//        }
//
//        `when`(
//            emailService.getbyUserIdAndPin(any(), anyString())
//        ).thenReturn(Optional.of(verification))
//
//        val ex = assertThrows(BadRequestException::class.java) {
//            userService.verifyOtp(OtpRequest(pin, userId))
//        }
//
//        assertEquals("Token expired, initiate another verification", ex.message)
//    }
//
//    @Test
//    fun `verifyOtp should fail when user not found`() {
//        val userId = UUID.randomUUID()
//        val pin = RandomString.make(6)
//
//        val verification = VerificationModel().apply {
//            this.userId = userId
//            this.type = VerificationType.REGISTER
//            ReflectionTestUtils.setField(this, "createdAt", LocalDateTime.now())
//        }
//
//        `when`(
//            emailService.getbyUserIdAndPin(any(), anyString())
//        ).thenReturn(Optional.of(verification))
//
//        `when`(userRepository.findById(any())).thenReturn(Optional.empty())
//
//        val ex = assertThrows(NotFoundException::class.java) {
//            userService.verifyOtp(OtpRequest(pin, userId))
//        }
//
//        assertEquals("User not found, verification cannot be completed", ex.message)
//    }
//
//    /* --------------------------------------------------------
//       HELPERS
//    --------------------------------------------------------- */
//
//    private fun verificationModel(user: User): VerificationModel =
//        VerificationModel().apply {
//            userId = user.id
//            pin = "123456"
//            mode = OtpModeEnum.EMAIL
//            type = VerificationType.REGISTER
//            messageId = RandomString.make(10)
//        }
//
//    companion object {
//        const val ACTIVATE_ACCOUNT = "Activate Trustline Account"
//    }
}
