package trustline.appuser.service;

import trustline.appuser.PagedResponse
import trustline.appuser.dto.*
import trustline.appuser.model.ProfileResponseDto
import trustline.appuser.model.UserModel
import trustline.appuser.model.UserResponseDto
import java.util.*

/**
 * Business-logic contract for user management.
 *
 * Handles registration, authentication, OTP verification, password reset,
 * user invitation, role/permission management, and profile operations.
 * All role and permission operations are scoped to the authenticated
 * administrator's institution to prevent cross-institution data leakage.
 */
interface UserService {
    /** Registers a new user and sends an OTP verification email. */
    fun createUser(user: RegisterUserDto): UserResponseDto

    /** Authenticates via credentials and returns a signed JWT (or triggers OTP flow if unverified). */
    fun login(loginReq: LoginReq): LoginRes

    /** Validates a one-time password and marks the account verified. */
    fun verifyOtp(otpRequest: OtpRequest): OtpVerificationResponse

    fun forgotPassword(forgotPasswordReq: ForgotPasswordReq): ForgotPasswordRes

    fun resetPassword(resetPasswordReq: ResetPasswordReq): UserResponseDto

    fun resendOtp(resendOtpRequest: ResendOtpRequest): OtpVerificationResponse

    fun getUserById(userId: UUID): UserModel

    /** Invites a new user to the institution with a temporary password sent by email. */
    fun inviteUser(inviteUserReq: InviteUserReq): InviteUserRes

    /** Replaces the user's current role (institution-scoped with global fallback). */
    fun changeUserRole(changeUserRoleReq: ChangeUserRoleReq): ChangeUserRoleRes

    /** Replaces the user's current role using roleId (institution-scoped with global fallback). */
    fun assignUserRoleById(req: AssignUserRoleByIdReq): ChangeUserRoleRes

    /** Returns all roles belonging to the authenticated user's institution with their permissions. */
    fun getAllRoles(): List<RoleDto>

    /** Returns institution-specific and globally-defined permissions combined. */
    fun getAllPermissions(): List<PermissionDto>

    /**
     * Creates a new role scoped to the authenticated administrator's institution.
     * Throws [DuplicateException] if a role with the same name already exists in the institution.
     */
    fun createRole(req: CreateRoleRequest): RoleDto

    /**
     * Creates a new permission scoped to the authenticated administrator's institution.
     * Throws [DuplicateException] if a permission with the same name already exists in the institution.
     */
    fun createPermission(req: CreatePermissionRequest): PermissionDto

    /**
     * Appends [AddPermissionsToRoleRequest.permissionIds] to an existing role.
     * The operation is additive — existing permissions on the role are preserved.
     * Throws [BadRequestException] if the role belongs to a different institution.
     * Throws [NotFoundException] if any permission ID does not exist.
     */
    fun addPermissionsToRole(roleId: UUID, req: AddPermissionsToRoleRequest): RoleDto

    fun getAllNonAdminUsersWithOngoingCases(): List<AdminUserListDto>

    fun getAllAdminUsersWithoutLoggedInUser(): List<AdminUserListDto>

    fun getFilteredNonAdminUsersWithOngoingCases(
        offset: Int,
        limit: Int,
        email: String?,
        firstName: String?,
        lastName: String?,
        gender: Gender?,
        verifiedStatus: Boolean?
    ): PagedResponse<AdminUserListDto>

    fun getNonAdminUserById(userId: UUID): AdminUserListDto

    fun updateProfile(request: UpdateProfileRequest, profileImage: org.springframework.web.multipart.MultipartFile?): ProfileResponseDto

    fun getProfile(): ProfileResponseDto
}
