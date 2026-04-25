package trustline.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import trustline.appuser.dto.*
import trustline.appuser.model.RoleModel
import trustline.appuser.model.PermissionModel
import trustline.appuser.repository.PermissionRepository
import trustline.appuser.repository.RolesRepository
import trustline.appuser.repository.UserRepository
import trustline.appuser.service.EmailService
import trustline.appuser.service.UserServiceImpl
import trustline.config.exception.BadRequestException
import trustline.config.exception.DuplicateException
import trustline.config.exception.NotFoundException
import trustline.config.security.AuthDetailsResponse
import trustline.config.security.JWTConfigService
import trustline.institution.model.InstitutionModel
import trustline.institution.service.InstitutionService
import com.cloudinary.Cloudinary
import java.util.*

/**
 * Unit tests for role and permission management methods in [UserServiceImpl].
 *
 * All operations are institution-scoped: data from one institution must not
 * be accessible or modifiable from another.
 */
class RolePermissionServiceTest {

    // ── collaborators ────────────────────────────────────────────────────────
    private val userRepository: UserRepository = mockk()
    private val rolesRepository: RolesRepository = mockk()
    private val permissionRepository: PermissionRepository = mockk()
    private val jwtConfigService: JWTConfigService = mockk()
    private val authenticationManager: AuthenticationManager = mockk()
    private val emailService: EmailService = mockk()
    private val institutionService: InstitutionService = mockk()
    private val cloudinary: Cloudinary = mockk()

    private lateinit var userService: UserServiceImpl

    // ── shared fixtures ──────────────────────────────────────────────────────
    private val institutionId: UUID = UUID.randomUUID()
    private val userId: UUID = UUID.randomUUID()
    private val institution = InstitutionModel(id = institutionId, name = "Acme Corp")
    private val authDetails = AuthDetailsResponse(userId, institutionId, "admin@acme.com")

    @BeforeEach
    fun setUp() {
        userService = UserServiceImpl(
            userRepository,
            rolesRepository,
            permissionRepository,
            jwtConfigService,
            authenticationManager,
            BCryptPasswordEncoder(),
            emailService,
            institutionService,
            cloudinary
        )
        every { jwtConfigService.getAuthDetails() } returns authDetails
        every { institutionService.getAuthUserInstitution() } returns institution
    }

    // ── createRole ────────────────────────────────────────────────────────────

    @Test
    fun `createRole should save and return RoleDto scoped to institution`() {
        val req = CreateRoleRequest(name = "Supervisor", description = "Reviews cases")
        val savedRole = RoleModel(
            id = UUID.randomUUID(),
            name = req.name,
            description = req.description,
            institution = institution
        )

        every { rolesRepository.findByNameAndInstitutionId(req.name, institutionId) } returns null
        every { rolesRepository.save(any()) } returns savedRole

        val result = userService.createRole(req)

        assertEquals("Supervisor", result.name)
        assertEquals(institutionId, result.institutionId)
        verify(exactly = 1) { rolesRepository.save(any()) }
    }

    @Test
    fun `createRole should throw DuplicateException when name already exists in institution`() {
        val req = CreateRoleRequest(name = "Supervisor", description = "already here")
        val existing = RoleModel(id = UUID.randomUUID(), name = "Supervisor", institution = institution)

        every { rolesRepository.findByNameAndInstitutionId(req.name, institutionId) } returns existing

        assertThrows<DuplicateException> { userService.createRole(req) }
        verify(exactly = 0) { rolesRepository.save(any()) }
    }

    // ── createPermission ──────────────────────────────────────────────────────

    @Test
    fun `createPermission should save and return PermissionDto`() {
        val req = CreatePermissionRequest(name = "EDIT_CASES", description = "Can edit cases")
        val savedPerm = PermissionModel(
            id = UUID.randomUUID(),
            name = req.name,
            description = req.description,
            institution = institution
        )

        every { permissionRepository.findByNameAndInstitutionId(req.name, institutionId) } returns null
        every { permissionRepository.save(any()) } returns savedPerm

        val result = userService.createPermission(req)

        assertEquals("EDIT_CASES", result.name)
        verify(exactly = 1) { permissionRepository.save(any()) }
    }

    @Test
    fun `createPermission should throw DuplicateException when name already exists`() {
        val req = CreatePermissionRequest(name = "EDIT_CASES", description = "duplicate")
        val existing = PermissionModel(id = UUID.randomUUID(), name = "EDIT_CASES", description = "old", institution = institution)

        every { permissionRepository.findByNameAndInstitutionId(req.name, institutionId) } returns existing

        assertThrows<DuplicateException> { userService.createPermission(req) }
        verify(exactly = 0) { permissionRepository.save(any()) }
    }

    // ── addPermissionsToRole ──────────────────────────────────────────────────

    @Test
    fun `addPermissionsToRole should attach permissions to role and return updated RoleDto`() {
        val roleId = UUID.randomUUID()
        val permId = UUID.randomUUID()
        val perm = PermissionModel(id = permId, name = "VIEW_REPORTS", description = "Read-only", institution = institution)
        val role = RoleModel(id = roleId, name = "Analyst", institution = institution)

        val req = AddPermissionsToRoleRequest(permissionIds = listOf(permId))

        every { rolesRepository.findById(roleId) } returns Optional.of(role)
        every { permissionRepository.findById(permId) } returns Optional.of(perm)
        every { rolesRepository.save(any()) } answers { firstArg() }

        val result = userService.addPermissionsToRole(roleId, req)

        assertEquals("Analyst", result.name)
        assertTrue(result.permissions.any { it.name == "VIEW_REPORTS" })
        verify(exactly = 1) { rolesRepository.save(role) }
    }

    @Test
    fun `addPermissionsToRole should throw BadRequestException when role belongs to different institution`() {
        val roleId = UUID.randomUUID()
        val otherId = UUID.randomUUID()
        val otherInstitution = InstitutionModel(id = otherId, name = "Other Corp")
        val role = RoleModel(id = roleId, name = "Foreign Role", institution = otherInstitution)

        every { rolesRepository.findById(roleId) } returns Optional.of(role)

        assertThrows<BadRequestException> {
            userService.addPermissionsToRole(roleId, AddPermissionsToRoleRequest(permissionIds = listOf(UUID.randomUUID())))
        }
    }

    @Test
    fun `addPermissionsToRole should throw NotFoundException when permission does not exist`() {
        val roleId = UUID.randomUUID()
        val missingPermId = UUID.randomUUID()
        val role = RoleModel(id = roleId, name = "Analyst", institution = institution)

        every { rolesRepository.findById(roleId) } returns Optional.of(role)
        every { permissionRepository.findById(missingPermId) } returns Optional.empty()

        assertThrows<NotFoundException> {
            userService.addPermissionsToRole(roleId, AddPermissionsToRoleRequest(permissionIds = listOf(missingPermId)))
        }
    }

    // ── getAllRoles ──────────────────────────────────────────────────────────

    @Test
    fun `getAllRoles should return institution-scoped roles with their permissions`() {
        val roles = listOf(
            RoleModel(id = UUID.randomUUID(), name = "Analyst", institution = institution),
            RoleModel(id = UUID.randomUUID(), name = "Supervisor", institution = institution)
        )
        every { rolesRepository.findAllByInstitutionIdWithPermissions(institutionId) } returns roles

        val result = userService.getAllRoles()

        assertEquals(2, result.size)
        assertTrue(result.all { it.institutionId == institutionId })
    }

    // ── getAllPermissions ────────────────────────────────────────────────────

    @Test
    fun `getAllPermissions should combine institution-specific and global permissions`() {
        val scopedPerm = PermissionModel(id = UUID.randomUUID(), name = "SCOPED", description = "inst specific", institution = institution)
        val globalPerm = PermissionModel(id = UUID.randomUUID(), name = "GLOBAL", description = "global perm")

        every { permissionRepository.findAllByInstitutionIdOrGlobal(institutionId) } returns listOf(scopedPerm, globalPerm)

        val result = userService.getAllPermissions()

        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "SCOPED" })
        assertTrue(result.any { it.name == "GLOBAL" })
    }
}
