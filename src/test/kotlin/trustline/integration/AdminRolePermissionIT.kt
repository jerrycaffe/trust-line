package trustline.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.hamcrest.Matchers.containsString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import trustline.appuser.dto.AddPermissionsToRoleRequest
import trustline.appuser.dto.CreatePermissionRequest
import trustline.appuser.dto.CreateRoleRequest
import trustline.config.security.AuthDetailsResponse
import trustline.config.security.JWTConfigService
import trustline.institution.model.InstitutionModel
import trustline.institution.repository.InstitutionRepository
import java.util.*

/**
 * Integration tests for the role and permission management endpoints.
 *
 * Uses a Testcontainers PostgreSQL database (via [BaseIT]) with Flyway
 * migrations applied on startup.  [JWTConfigService] is replaced with a
 * Mockito mock so that [AuthDetailsResponse] returns the test institution.
 *
 * All request go through the full Spring MVC stack including security filters.
 * The JWT filter bypasses token validation when no `Authorization` header is
 * present, so `@WithMockUser` can seed the [SecurityContext] safely.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = ["Administrator"])
class AdminRolePermissionIT : BaseIT() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var institutionRepository: InstitutionRepository

    @MockBean
    private lateinit var jwtConfigService: JWTConfigService

    private lateinit var institution: InstitutionModel
    private val userId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        // Persist a unique institution per test-class run so other tests don't interfere
        institution = institutionRepository.save(
            InstitutionModel(name = "IT Corp ${UUID.randomUUID()}")
        )
        given(jwtConfigService.getAuthDetails())
            .willReturn(AuthDetailsResponse(userId, institution.id!!, "admin@it-corp.com"))
    }

    // ── POST /roles ───────────────────────────────────────────────────────────

    @Test
    fun `POST roles returns 201 and persists role scoped to institution`() {
        val req = CreateRoleRequest(name = "Counsellor-${UUID.randomUUID()}", description = "Handles sessions")

        mockMvc.perform(
            post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.name").value(req.name))
            .andExpect(jsonPath("$.data.institutionId").value(institution.id!!.toString()))
            .andExpect(jsonPath("$.data.permissions").isArray)
    }

    @Test
    fun `POST roles returns 409 when role name already exists in institution`() {
        val name = "Duplicate-${UUID.randomUUID()}"
        val req = CreateRoleRequest(name = name, description = "First")

        // Create once
        mockMvc.perform(
            post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isCreated)

        // Create again — should conflict
        mockMvc.perform(
            post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isConflict)
    }

    @Test
    @WithAnonymousUser
    fun `POST roles returns 403 for unauthenticated request`() {
        val req = CreateRoleRequest(name = "UnAuth-${UUID.randomUUID()}", description = "No auth")

        mockMvc.perform(
            post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(authorities = ["MANAGE_ROLES"])
    fun `POST roles returns 201 for user with MANAGE_ROLES permission`() {
        val req = CreateRoleRequest(name = "ScopedRole-${UUID.randomUUID()}", description = "Permission-based access")

        mockMvc.perform(
            post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.name").value(req.name))
    }

    // ── POST /permissions ─────────────────────────────────────────────────────

    @Test
    fun `POST permissions returns 201 and persists permission`() {
        val req = CreatePermissionRequest(name = "VIEW_CASES-${UUID.randomUUID()}", description = "Read-only case access")

        mockMvc.perform(
            post("/api/v1/admin/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.name").value(req.name))
            .andExpect(jsonPath("$.data.institutionId").value(institution.id!!.toString()))
    }

    // ── GET /roles ────────────────────────────────────────────────────────────

    @Test
    fun `GET roles returns 200 and lists roles for the institution`() {
        val roleName = "Analyst-${UUID.randomUUID()}"
        // Create a role first so the list is non-empty
        mockMvc.perform(
            post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateRoleRequest(name = roleName, description = "Data analyst")))
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/api/v1/admin/roles"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(content().string(containsString(roleName)))
    }

    // ── POST /roles/{id}/permissions ──────────────────────────────────────────

    @Test
    fun `POST roles-id-permissions attaches permission to role`() {
        val roleName = "PermRole-${UUID.randomUUID()}"
        val permName = "EDIT_CASES-${UUID.randomUUID()}"

        // Create role
        val roleResult = mockMvc.perform(
            post("/api/v1/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateRoleRequest(name = roleName, description = "Role desc")))
        ).andReturn()
        val roleId = objectMapper
            .readTree(roleResult.response.contentAsString)
            .get("data").get("id").asText()

        // Create permission
        val permResult = mockMvc.perform(
            post("/api/v1/admin/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreatePermissionRequest(name = permName, description = "Edit perm")))
        ).andReturn()
        val permId = objectMapper
            .readTree(permResult.response.contentAsString)
            .get("data").get("id").asText()

        // Add permission to role
        mockMvc.perform(
            post("/api/v1/admin/roles/$roleId/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddPermissionsToRoleRequest(permissionIds = listOf(UUID.fromString(permId)))))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.permissions").isArray)
            .andExpect(jsonPath("$.data.permissions[0].name").value(permName))
    }
}
