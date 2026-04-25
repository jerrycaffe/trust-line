package trustline.appuser.service;


import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import trustline.appuser.Utility
import trustline.appuser.dto.*
import trustline.appuser.model.*
import trustline.appuser.repository.PermissionRepository
import trustline.appuser.repository.RolesRepository
import trustline.appuser.repository.UserRepository
import trustline.config.exception.BadRequestException
import trustline.config.exception.DuplicateException
import trustline.config.exception.EmailNotFoundException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.service.InstitutionService
import trustline.notification.model.VerificationModel
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val rolesRepository: RolesRepository,
    private val permissionRepository: PermissionRepository,
    private val jwtConfig: JWTConfigService,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder(),
    private val emailService: EmailService,
    private val institutionService: InstitutionService,
    private val cloudinary: Cloudinary
) : UserService {

    private val log = KotlinLogging.logger {}

    val ACTIVATE_ACCOUNT = "Activate Trustline Account"
    val RESET_PASSWORD = "Reset Password";
    val RESEND_OTP = "Trustline Resend OTP";
    val PREV_VERIFICATION_NOT_FOUND = "No Previous verification found";

    override fun createUser(user: RegisterUserDto): UserResponseDto {
        val institution = institutionService.getInstitutionById(user.institutionId)
            ?: throw NotFoundException("institution does not exist")
        val prevUser = userRepository.findByEmailOrPhoneNumberAndInstitutionId(
            user.email!!,
            user.phoneNumber!!,
            user.institutionId
        )

        if (prevUser?.email.equals(user.email)) throw DuplicateException("Email already exist, Please login")
        if (prevUser?.phoneNumber.equals(user.phoneNumber)) throw DuplicateException("Phone number already exist, Please login")

        val userRole = rolesRepository.findByNameAndInstitutionIdIsNull("User") ?: throw NotFoundException("Default user role not found")
        val userModel = user.toUserModel(passwordEncoder.encode(user.password), institution)
        userModel.roles.add(userRole)
        val newUser = userRepository.save(userModel)
        val emailVerification: VerificationModel? =
            generateOtp(newUser, generateOtpPin(), ACTIVATE_ACCOUNT, VerificationType.REGISTER)

        return newUser.toResponseDto(emailVerification?.id)
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
            user,
            otp,
            verificationType,
            Status.UNVERIFIED
        )
    }


    @Transactional
    override fun login(loginReq: LoginReq): LoginRes {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginReq.userName, loginReq.password)
        )

        val user = userRepository.findByEmailAndInstitutionId(loginReq.userName!!, loginReq.institutionId!!)
            ?: throw UsernameNotFoundException(loginReq.userName)

        if (user.status == Status.OTP_VALIDATION) {
            val otp = generateOtp(user, generateOtpPin(), ACTIVATE_ACCOUNT, VerificationType.REGISTER)
            return LoginRes(otpId = otp?.id, userId = user.id)
        }

        val roleNames = userRepository.findRoleNamesByUserId(user.id!!)
        val token = jwtConfig.generateToken(user, roleNames)
        return LoginRes(token, user.toResponseDto())
    }

    override fun verifyOtp(otpRequest: OtpRequest): OtpVerificationResponse {
        log.info { "otp request received with details $otpRequest" }
        val verifyUser: VerificationModel =
            emailService.getbyUserIdAndPinAndStatus(otpRequest.userId!!, otpRequest.verificationId!!, Status.UNVERIFIED)
                ?: throw NotFoundException(
                    PREV_VERIFICATION_NOT_FOUND
                )

//        Update user status if it otp is for user verification
        if (verifyUser.type!! == VerificationType.REGISTER) {
            verifyUserRegistration(otpRequest.userId)
        }
        verifyUser.status = Status.VERIFIED
        emailService.updateVerification(verifyUser)
        return OtpVerificationResponse("Verification Successful", otpId = verifyUser.id);
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
            .findByEmailAndInstitutionId(forgotPasswordReq.email!!, forgotPasswordReq.institutionId!!)
            ?: throw BadRequestException("You cannot perform this action as user does not exist")


        val generatedOtp = generateOtp(
            user,
            generateOtpPin(),
            RESET_PASSWORD,
            VerificationType.RESET_PASSWORD
        )

        return ForgotPasswordRes(generatedOtp?.id, user.id)
    }

    override fun resetPassword(resetPasswordReq: ResetPasswordReq): UserResponseDto {

        emailService.getVerificationByIdAndStatus(resetPasswordReq.tokenId!!, Status.VERIFIED)
            ?: throw NotFoundException("Previous token was not found")

        val user =
            userRepository.findByEmailAndInstitutionId(resetPasswordReq.userName!!, resetPasswordReq.institutionId!!)
                ?: throw EmailNotFoundException(resetPasswordReq.userName)

        val newPassword = passwordEncoder.encode(resetPasswordReq.newPassword)
        user.password = newPassword

        return userRepository.save(user).toResponseDto()
    }


    override fun resendOtp(resendOtpRequest: ResendOtpRequest): OtpVerificationResponse {

        val previousVerification = emailService
            .getVerificationById(resendOtpRequest.prevOtpId!!)
            .orElseThrow { NotFoundException(PREV_VERIFICATION_NOT_FOUND) }

        val newOtp = generateOtp(
            previousVerification.user,
            generateOtpPin(),
            RESEND_OTP,
            previousVerification.type!!
        )

        return OtpVerificationResponse(
            "Resend OTP Successful",
            newOtp?.id
        )
    }

    override fun getUserById(userId: UUID): UserModel {
        return userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }
    }

    override fun inviteUser(inviteUserReq: InviteUserReq): InviteUserRes {
        val institution = institutionService.getAuthUserInstitution()

        val existingUser = userRepository.findByEmailAndInstitutionId(inviteUserReq.email!!, institution.id!!)
        if (existingUser != null) throw DuplicateException("User with this email already exists in this institution")

        val role = rolesRepository.findByNameForInstitution(inviteUserReq.role!!, institution.id!!)
            .maxByOrNull { it.institution != null }
            ?: throw NotFoundException("Role '${inviteUserReq.role}' not found")

        val tempPassword = UUID.randomUUID().toString().take(12)

        val user = UserModel(
            email = inviteUserReq.email,
            password = passwordEncoder.encode(tempPassword),
            institution = institution,
            authProvider = AuthProvider.LOCAL,
            status = Status.VERIFIED,
            isAccountVerified = true
        )
        user.roles.add(role)
        val savedUser = userRepository.save(user)

        val emailRequest = EmailRequest(
            recipientName = inviteUserReq.email,
            recipientEmail = inviteUserReq.email,
            recipientId = savedUser.id,
            htmlTemplate = Utility.inviteEmailTemplate(inviteUserReq.email, tempPassword, role.name!!),
            subject = "You've been invited to Trustline"
        )
        emailService.sendMail(emailRequest)

        return InviteUserRes(
            userId = savedUser.id!!,
            email = savedUser.email,
            role = role.name!!
        )
    }

    @Transactional
    override fun changeUserRole(changeUserRoleReq: ChangeUserRoleReq): ChangeUserRoleRes {
        val user = userRepository.findById(changeUserRoleReq.userId!!)
            .orElseThrow { NotFoundException("User not found") }

        val institution = institutionService.getAuthUserInstitution()
        val newRole = rolesRepository.findByNameAndInstitutionId(changeUserRoleReq.role!!, institution.id!!)
            ?: rolesRepository.findByNameAndInstitutionIdIsNull(changeUserRoleReq.role!!)
            ?: throw NotFoundException("Role '${changeUserRoleReq.role}' not found")

        val previousRole = user.roles.firstOrNull()?.name ?: "None"

        user.roles.clear()
        user.roles.add(newRole)
        userRepository.save(user)

        return ChangeUserRoleRes(
            userId = user.id!!,
            email = user.email,
            previousRole = previousRole,
            newRole = newRole.name!!
        )
    }

    override fun getAllRoles(): List<RoleDto> {
        val institution = institutionService.getAuthUserInstitution()
        return rolesRepository.findAllByInstitutionIdWithPermissions(institution.id!!).map { role ->
            RoleDto(
                id = role.id!!,
                name = role.name!!,
                description = role.description,
                institutionId = role.institution?.id,
                permissions = role.permissions.map { perm ->
                    PermissionDto(id = perm.id!!, name = perm.name, description = perm.description, institutionId = perm.institution?.id)
                }
            )
        }
    }

    override fun getAllPermissions(): List<PermissionDto> {
        val institution = institutionService.getAuthUserInstitution()
        return permissionRepository.findAllByInstitutionIdOrGlobal(institution.id!!).map { perm ->
            PermissionDto(id = perm.id!!, name = perm.name, description = perm.description, institutionId = perm.institution?.id)
        }
    }

    @Transactional
    override fun createRole(req: CreateRoleRequest): RoleDto {
        val institution = institutionService.getAuthUserInstitution()
        if (rolesRepository.findByNameAndInstitutionId(req.name, institution.id!!) != null)
            throw DuplicateException("Role '${req.name}' already exists in this institution")
        val saved = rolesRepository.save(
            RoleModel(name = req.name, description = req.description, institution = institution)
        )
        return RoleDto(id = saved.id!!, name = saved.name!!, description = saved.description, institutionId = saved.institution?.id, permissions = emptyList())
    }

    @Transactional
    override fun createPermission(req: CreatePermissionRequest): PermissionDto {
        val institution = institutionService.getAuthUserInstitution()
        if (permissionRepository.findByNameAndInstitutionId(req.name, institution.id!!) != null)
            throw DuplicateException("Permission '${req.name}' already exists in this institution")
        val saved = permissionRepository.save(
            PermissionModel(name = req.name, description = req.description, institution = institution)
        )
        return PermissionDto(id = saved.id!!, name = saved.name, description = saved.description, institutionId = saved.institution?.id)
    }

    @Transactional
    override fun addPermissionsToRole(roleId: UUID, req: AddPermissionsToRoleRequest): RoleDto {
        val institution = institutionService.getAuthUserInstitution()
        val role = rolesRepository.findById(roleId)
            .orElseThrow { NotFoundException("Role not found") }
        if (role.institution?.id != institution.id)
            throw BadRequestException("Role does not belong to your institution")
        val permissions = req.permissionIds.map { permId ->
            permissionRepository.findById(permId)
                .orElseThrow { NotFoundException("Permission '$permId' not found") }
        }
        role.permissions.addAll(permissions)
        val saved = rolesRepository.save(role)
        return RoleDto(
            id = saved.id!!,
            name = saved.name!!,
            description = saved.description,
            institutionId = saved.institution?.id,
            permissions = saved.permissions.map { perm ->
                PermissionDto(id = perm.id!!, name = perm.name, description = perm.description, institutionId = perm.institution?.id)
            }
        )
    }

    @Transactional
    override fun updateProfile(request: UpdateProfileRequest, profileImage: MultipartFile?): ProfileResponseDto {
        val authDetails = jwtConfig.getAuthDetails()
        val user = userRepository.findById(authDetails.userId)
            .orElseThrow { NotFoundException("User not found") }

        if (request.firstName != null) user.firstName = request.firstName
        if (request.lastName != null) user.lastName = request.lastName
        if (request.phoneNumber != null) user.phoneNumber = request.phoneNumber
        if (request.gender != null) user.gender = request.gender

        if (profileImage != null) {
            val maxSize = 5 * 1024 * 1024
            if (profileImage.size > maxSize) {
                throw BadRequestException("Profile image size exceeds 5MB limit")
            }
            val allowedTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp")
            if (profileImage.contentType !in allowedTypes) {
                throw BadRequestException("Invalid image type. Allowed: JPEG, PNG, GIF, WEBP")
            }
            val uploadResult = cloudinary.uploader().upload(
                profileImage.bytes,
                ObjectUtils.asMap(
                    "folder", "trustline/profiles",
                    "resource_type", "image"
                )
            )
            user.profileImageUrl = uploadResult["secure_url"] as String
        }

        userRepository.save(user)
        return user.toProfileResponse()
    }

    override fun getProfile(): ProfileResponseDto {
        val authDetails = jwtConfig.getAuthDetails()
        val user = userRepository.findById(authDetails.userId)
            .orElseThrow { NotFoundException("User not found") }
        return user.toProfileResponse()
    }
}
