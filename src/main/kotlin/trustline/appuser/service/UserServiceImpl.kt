package trustline.appuser.service;


import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import trustline.appuser.PageRequest
import trustline.appuser.PagedResponse
import trustline.appuser.Utility
import trustline.appuser.dto.*
import trustline.appuser.model.*
import trustline.appuser.repository.PermissionRepository
import trustline.appuser.repository.RolesRepository
import trustline.appuser.repository.UserRepository
import trustline.cases.repository.CaseRepository
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
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder(),
    private val emailService: EmailService,
    private val institutionService: InstitutionService,
    private val cloudinary: Cloudinary,
    private val caseRepository: CaseRepository
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

        val userRole = rolesRepository.findByNameAndInstitutionIdIsNull("User")
            ?: throw NotFoundException("Default user role not found")
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
        // Avoid role/permission fetch joins for login to prevent duplicate-row
        // single-result issues when roles have multiple permissions.
        val user = userRepository.findByEmailAndInstitutionId(loginReq.userName!!, loginReq.institutionId!!)
            ?: throw EmailNotFoundException(loginReq.userName)

        val storedPassword = user.password
        if (storedPassword.isNullOrBlank() || !passwordEncoder.matches(loginReq.password, storedPassword)) {
            throw BadRequestException("Invalid Credentials")
        }

        if (user.status == Status.OTP_VALIDATION) {
            val otp = generateOtp(user, generateOtpPin(), ACTIVATE_ACCOUNT, VerificationType.REGISTER)
            return LoginRes(otpId = otp?.id, userId = user.id)
        }

        val roleNames = userRepository.findRoleNamesByUserId(user.id!!)

        // Only role names are embedded in the JWT. Permissions are resolved
        // server-side from the role on every authenticated request, so admins
        // can change a role's permissions without invalidating issued tokens.
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

        emailService.getVerificationByIdAndStatus(resetPasswordReq.otpId!!, Status.VERIFIED)
            ?: throw NotFoundException("Previous token was not found")

        val user =
            userRepository.findByIdAndInstitutionId(resetPasswordReq.userId!!, resetPasswordReq.institutionId!!)
                ?: throw EmailNotFoundException("")

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
        val authDetails = jwtConfig.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val user = userRepository.findById(changeUserRoleReq.userId!!)
            .orElseThrow { NotFoundException("User not found") }

        if (user.id == authDetails.userId) {
            throw BadRequestException("You cannot change your own role")
        }

        if (user.institution.id != institution.id) {
            throw BadRequestException("User does not belong to your institution")
        }

        val newRole = rolesRepository.findByNameAndInstitutionId(changeUserRoleReq.role!!, institution.id!!)
            ?: rolesRepository.findByNameAndInstitutionIdIsNull(changeUserRoleReq.role!!)
            ?: throw NotFoundException("Role '${changeUserRoleReq.role}' not found")

        val previousRole = user.roles.firstOrNull()?.name ?: "None"

        // No-op reassignment should not attempt a DB write to avoid duplicate join-row issues.
        if (user.roles.size == 1 && user.roles.any { it.id == newRole.id }) {
            return ChangeUserRoleRes(
                userId = user.id!!,
                email = user.email,
                previousRole = previousRole,
                newRole = newRole.name!!,
                roles = user.roles.mapNotNull { it.name }.distinct()
            )
        }

        user.roles.clear()
        user.roles.add(newRole)
        val savedUser = userRepository.save(user)

        return ChangeUserRoleRes(
            userId = savedUser.id!!,
            email = savedUser.email,
            previousRole = previousRole,
            newRole = newRole.name!!,
            roles = savedUser.roles.mapNotNull { it.name }.distinct()
        )
    }

    @Transactional
    override fun assignUserRoleById(req: AssignUserRoleByIdReq): ChangeUserRoleRes {
        val authDetails = jwtConfig.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val user = userRepository.findById(req.userId!!)
            .orElseThrow { NotFoundException("User not found") }

        if (user.id == authDetails.userId) {
            throw BadRequestException("You cannot change your own role")
        }

        if (user.institution.id != institution.id) {
            throw BadRequestException("User does not belong to your institution")
        }

        val role = rolesRepository.findById(req.roleId!!)
            .orElseThrow { NotFoundException("Role not found") }

        if (role.institution?.id != null && role.institution?.id != institution.id) {
            throw BadRequestException("Role does not belong to your institution")
        }

        val previousRole = user.roles.firstOrNull()?.name ?: "None"

        // No-op reassignment should not attempt a DB write to avoid duplicate join-row issues.
        if (user.roles.size == 1 && user.roles.any { it.id == role.id }) {
            return ChangeUserRoleRes(
                userId = user.id!!,
                email = user.email,
                previousRole = previousRole,
                newRole = role.name ?: "",
                roles = user.roles.mapNotNull { it.name }.distinct()
            )
        }

        user.roles.clear()
        user.roles.add(role)
        val savedUser = userRepository.save(user)

        return ChangeUserRoleRes(
            userId = savedUser.id!!,
            email = savedUser.email,
            previousRole = previousRole,
            newRole = role.name ?: "",
            roles = savedUser.roles.mapNotNull { it.name }.distinct()
        )
    }

    override fun getAllRoles(): List<RoleDto> {
        val institution = institutionService.getAuthUserInstitution()
        return rolesRepository.findAllByInstitutionIdWithPermissions(institution.id!!).map { role ->
            RoleDto(
                id = role.id!!,
                name = role.name!!,
                description = role.description,
                institutionId = role.institutionId,
                permissions = role.permissions.map { perm ->
                    PermissionDto(
                        id = perm.id!!,
                        name = perm.name,
                        description = perm.description,
                        institutionId = perm.institutionId
                    )
                }
            )
        }
    }

    override fun getAllPermissions(): List<PermissionDto> {
        val institution = institutionService.getAuthUserInstitution()
        return permissionRepository.findAllByInstitutionIdOrGlobal(institution.id!!).map { perm ->
            PermissionDto(
                id = perm.id!!,
                name = perm.name,
                description = perm.description,
                institutionId = perm.institutionId
            )
        }
    }

    @Transactional
    override fun createRole(req: CreateRoleRequest): RoleDto {
        val institution = institutionService.getAuthUserInstitution()
        if (rolesRepository.findByNameAndInstitutionId(req.name, institution.id!!) != null)
            throw DuplicateException("Role '${req.name}' already exists in this institution")
        if (rolesRepository.findByNameAndInstitutionIdIsNull(req.name) != null)
            throw DuplicateException("Role '${req.name}' is reserved as a global role")
        val saved = rolesRepository.save(
            RoleModel(name = req.name, description = req.description, institution = institution)
        )
        return RoleDto(
            id = saved.id!!,
            name = saved.name!!,
            description = saved.description,
            institutionId = institution.id,
            permissions = emptyList()
        )
    }

    @Transactional
    override fun createPermission(req: CreatePermissionRequest): PermissionDto {
        val institution = institutionService.getAuthUserInstitution()
        if (permissionRepository.findByNameAndInstitutionId(req.name, institution.id!!) != null)
            throw DuplicateException("Permission '${req.name}' already exists in this institution")
        if (permissionRepository.findByNameAndInstitutionIdIsNull(req.name) != null)
            throw DuplicateException("Permission '${req.name}' is reserved as a global permission")
        val saved = permissionRepository.save(
            PermissionModel(name = req.name, description = req.description, institution = institution)
        )
        return PermissionDto(
            id = saved.id!!,
            name = saved.name,
            description = saved.description,
            institutionId = institution.id
        )
    }

    @Transactional
    override fun addPermissionsToRole(roleId: UUID, req: AddPermissionsToRoleRequest): RoleDto {
        val institution = institutionService.getAuthUserInstitution()
        val role = rolesRepository.findById(roleId)
            .orElseThrow { NotFoundException("Role not found") }
        if (role.institution?.id != institution.id)
            throw BadRequestException("Role does not belong to your institution")
        val permissions = req.permissionIds.map { permId ->
            val permission = permissionRepository.findById(permId)
                .orElseThrow { NotFoundException("Permission '$permId' not found") }

            if (permission.institution?.id != null && permission.institution.id != institution.id) {
                throw BadRequestException("Permission '$permId' does not belong to your institution")
            }

            permission
        }
        role.permissions.addAll(permissions)
        val saved = rolesRepository.save(role)
        return RoleDto(
            id = saved.id!!,
            name = saved.name!!,
            description = saved.description,
            institutionId = saved.institutionId,
            permissions = saved.permissions.map { perm ->
                PermissionDto(
                    id = perm.id!!,
                    name = perm.name,
                    description = perm.description,
                    institutionId = perm.institutionId
                )
            }
        )
    }

    @Transactional
    override fun getAllNonAdminUsersWithOngoingCases(): List<AdminUserListDto> {
        val institution = institutionService.getAuthUserInstitution()
        val institutionId = institution.id ?: throw NotFoundException("Institution not found")
        val users = userRepository.findNonAdminUsersByInstitutionId(institutionId)

        return users.map { user -> toAdminUserListDto(user, institutionId) }
    }

    @Transactional
    override fun getAllAdminUsersWithoutLoggedInUser(): List<AdminUserListDto> {
        val authDetails = jwtConfig.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val institutionId = institution.id ?: throw NotFoundException("Institution not found")
        val users = userRepository.findAdminUsersByInstitutionIdExcludingCurrentUser(institutionId, authDetails.userId)

        return users.map { user -> toAdminUserListDto(user, institutionId) }
    }

    @Transactional
    override fun getFilteredNonAdminUsersWithOngoingCases(
        offset: Int,
        limit: Int,
        email: String?,
        firstName: String?,
        lastName: String?,
        gender: Gender?,
        verifiedStatus: Boolean?
    ): PagedResponse<AdminUserListDto> {
        val institution = institutionService.getAuthUserInstitution()
        val institutionId = institution.id ?: throw NotFoundException("Institution not found")

        val normalizedEmail = email?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }?.let { "%$it%" }
        val normalizedFirstName = firstName?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }?.let { "%$it%" }
        val normalizedLastName = lastName?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }?.let { "%$it%" }

        val usersPage = userRepository.findNonAdminUsersByInstitutionIdAndFilters(
            institutionId,
            normalizedEmail,
            normalizedFirstName,
            normalizedLastName,
            gender,
            verifiedStatus,
            PageRequest(offset, limit, Sort.by("createdAt").descending())
        )

        return PagedResponse(usersPage.map { user -> toAdminUserListDto(user, institutionId) })
    }

    @Transactional
    override fun getNonAdminUserById(userId: UUID): AdminUserListDto {
        val institution = institutionService.getAuthUserInstitution()
        val institutionId = institution.id ?: throw NotFoundException("Institution not found")
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("User not found") }

        if (user.institution.id != institutionId) {
            throw NotFoundException("User not found in your institution")
        }

      


        return toAdminUserListDto(user, institutionId)
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

    private fun toAdminUserListDto(user: UserModel, institutionId: UUID): AdminUserListDto {
        val userId = user.id ?: throw NotFoundException("User id not found")
        val ongoingCases = caseRepository.countByUserIdAndInstitutionIdAndIsDeletedFalseAndIsClosedFalse(
            userId,
            institutionId
        )

        return AdminUserListDto(
            userId = userId,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber,
            status = user.status,
            roles = user.roles.mapNotNull { it.name }.distinct(),
            unit = user.unit?.name,
            gender = user.gender,
            createdAt = user.createdAt,
            ongoingCases = ongoingCases
        )
    }
}

