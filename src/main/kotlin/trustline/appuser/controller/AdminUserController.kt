package trustline.appuser.controller

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.appuser.PagedResponse
import trustline.appuser.dto.*
import trustline.appuser.service.UserService
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_PERMISSIONS
import trustline.config.security.PermissionAuthorities.MANAGE_ROLES
import trustline.config.security.PermissionAuthorities.MANAGE_USERS
import java.util.*

/**
 * REST controller for administrator-only user and access-control operations.
 *
 * All endpoints require the **Administrator** authority.
 * Base path: `api/v1/admin/`
 *
 * Roles and permissions are automatically scoped to the authenticated
 * administrator's institution so that cross-institution data leakage
 * is prevented at the service layer.
 */
@RestController
@RequestMapping("api/v1/admin/")
class AdminUserController(
    private val userService: UserService
) {

    /** Invites a new user to the institution with a temporary password sent by email. */
    @PostMapping("/invite")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_USERS')")
    fun inviteUser(@Validated @RequestBody inviteUserReq: InviteUserReq): InviteUserRes {
        return userService.inviteUser(inviteUserReq)
    }

    /** Replaces the user's current role with the specified role (institution-scoped with global fallback). */
    @PutMapping("/users/role")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_USERS')")
    fun changeUserRole(@Validated @RequestBody changeUserRoleReq: ChangeUserRoleReq): ChangeUserRoleRes {
        return userService.changeUserRole(changeUserRoleReq)
    }

    @PutMapping("/users/roles/assign")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_USERS')")
    fun assignUserRoleById(@Validated @RequestBody req: AssignUserRoleByIdReq): ChangeUserRoleRes {
        return userService.assignUserRoleById(req)
    }

    /** Lists all roles belonging to the caller's institution (with their permissions). */
    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ROLES')")
    fun getAllRoles(): List<RoleDto> {
        return userService.getAllRoles()
    }

    /** Lists institution-specific permissions plus globally-defined permissions. */
    @GetMapping("/permissions")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_PERMISSIONS')")
    fun getAllPermissions(): List<PermissionDto> {
        return userService.getAllPermissions()
    }

    /** Lists non-admin users (paginated); applies optional filters when provided. */
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_USERS')")
    fun getAllNonAdminUsers(
        @RequestParam(value = "offset") offset: Int? = 0,
        @RequestParam(value = "limit") limit: Int? = 20,
        @RequestParam(value = "email", required = false) email: String? = null,
        @RequestParam(value = "firstName", required = false) firstName: String? = null,
        @RequestParam(value = "firstname", required = false) firstNameLower: String? = null,
        @RequestParam(value = "lastName", required = false) lastName: String? = null,
        @RequestParam(value = "lastname", required = false) lastNameLower: String? = null,
        @RequestParam(value = "gender", required = false) gender: Gender? = null,
        @RequestParam(value = "verifiedStatus", required = false) verifiedStatus: Boolean? = null,
        @RequestParam(value = "verifiedstatus", required = false) verifiedStatusLower: Boolean? = null,
    ): PagedResponse<AdminUserListDto> {
        val effectiveFirstName = firstName ?: firstNameLower
        val effectiveLastName = lastName ?: lastNameLower
        val effectiveVerifiedStatus = verifiedStatus ?: verifiedStatusLower

        return userService.getFilteredNonAdminUsersWithOngoingCases(
            offset!!,
            limit!!,
            email,
            effectiveFirstName,
            effectiveLastName,
            gender,
            effectiveVerifiedStatus
        )
    }

    /** Lists admin users in the institution, excluding the currently logged-in admin. */
    @GetMapping("/admins")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_USERS')")
    fun getAllAdminUsers(): List<AdminUserListDto> {
        return userService.getAllAdminUsersWithoutLoggedInUser()
    }

    /** Retrieves a single non-admin user by ID with ongoing case count. */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_USERS')")
    fun getAdminUserById(@PathVariable id: UUID): AdminUserListDto {
        return userService.getNonAdminUserById(id)
    }

    /**
     * Creates a new role scoped to the caller's institution.
     * Returns HTTP 201 CREATED with the persisted [RoleDto].
     * Returns HTTP 409 CONFLICT if a role with the same name already exists in the institution.
     */
    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ROLES')")
    fun createRole(@Validated @RequestBody req: CreateRoleRequest): RoleDto {
        return userService.createRole(req)
    }

    /**
     * Creates a new permission scoped to the caller's institution.
     * Returns HTTP 201 CREATED with the persisted [PermissionDto].
     * Returns HTTP 409 CONFLICT if a permission with the same name already exists in the institution.
     */
    @PostMapping("/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_PERMISSIONS')")
    fun createPermission(@Validated @RequestBody req: CreatePermissionRequest): PermissionDto {
        return userService.createPermission(req)
    }

    /**
     * Appends the supplied permissions to an existing role.
     * The operation is additive: permissions already on the role are kept.
     * Returns HTTP 400 if the role belongs to a different institution.
     * Returns HTTP 404 if the role or any permission ID does not exist.
     */
    @PostMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ROLES')")
    fun addPermissionsToRole(
        @PathVariable roleId: UUID,
        @Validated @RequestBody req: AddPermissionsToRoleRequest
    ): RoleDto {
        return userService.addPermissionsToRole(roleId, req)
    }
}

