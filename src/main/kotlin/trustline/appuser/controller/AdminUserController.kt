package trustline.appuser.controller

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.appuser.dto.*
import trustline.appuser.service.UserService
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
@PreAuthorize("hasAuthority('Administrator')")
class AdminUserController(
    private val userService: UserService
) {

    /** Invites a new user to the institution with a temporary password sent by email. */
    @PostMapping("/invite")
    fun inviteUser(@Validated @RequestBody inviteUserReq: InviteUserReq): InviteUserRes {
        return userService.inviteUser(inviteUserReq)
    }

    /** Replaces the user's current role with the specified role (institution-scoped with global fallback). */
    @PutMapping("/users/role")
    fun changeUserRole(@Validated @RequestBody changeUserRoleReq: ChangeUserRoleReq): ChangeUserRoleRes {
        return userService.changeUserRole(changeUserRoleReq)
    }

    /** Lists all roles belonging to the caller's institution (with their permissions). */
    @GetMapping("/roles")
    fun getAllRoles(): List<RoleDto> {
        return userService.getAllRoles()
    }

    /** Lists institution-specific permissions plus globally-defined permissions. */
    @GetMapping("/permissions")
    fun getAllPermissions(): List<PermissionDto> {
        return userService.getAllPermissions()
    }

    /**
     * Creates a new role scoped to the caller's institution.
     * Returns HTTP 201 CREATED with the persisted [RoleDto].
     * Returns HTTP 409 CONFLICT if a role with the same name already exists in the institution.
     */
    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
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
    fun addPermissionsToRole(
        @PathVariable roleId: UUID,
        @Validated @RequestBody req: AddPermissionsToRoleRequest
    ): RoleDto {
        return userService.addPermissionsToRole(roleId, req)
    }
}
