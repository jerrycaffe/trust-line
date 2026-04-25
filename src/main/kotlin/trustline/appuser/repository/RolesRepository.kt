package trustline.appuser.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trustline.appuser.model.RoleModel
import java.util.*

/**
 * Spring Data JPA repository for [RoleModel] entities.
 *
 * Custom queries support the institution-scoping strategy:
 * global (no-institution) roles are always discoverable alongside
 * institution-specific ones when resolving a role for a user action.
 */
@Repository
interface RolesRepository : JpaRepository<RoleModel, UUID> {
    /** Global role lookup (institution is null) — used for default system roles such as "User". */
    fun findByName(name: String): RoleModel?
    /** Finds a role by name scoped to a specific institution. */
    fun findByNameAndInstitutionId(name: String, institutionId: UUID): RoleModel?
    /** Finds a global role (institution_id IS NULL) by name — used for system-seeded roles. */
    fun findByNameAndInstitutionIdIsNull(name: String): RoleModel?

    /** Finds a role by name within the institution, falling back to a global (no-institution) role. */
    /**
     * Finds roles matching [name] that are either scoped to [institutionId] or global.
     * Returns a list so the caller can prefer the institution-specific record (higher specificity)
     * over the global fallback using [maxByOrNull].
     */
    @Query("SELECT r FROM RoleModel r WHERE r.name = :name AND (r.institution.id = :institutionId OR r.institution IS NULL)")
    fun findByNameForInstitution(@Param("name") name: String, @Param("institutionId") institutionId: UUID): List<RoleModel>

    /** Fetches all roles (global + all institutions) with their permissions eagerly loaded. */
    @Query("SELECT DISTINCT r FROM RoleModel r LEFT JOIN FETCH r.permissions")
    fun findAllWithPermissions(): List<RoleModel>

    /** Fetches only the roles belonging to [institutionId] with their permissions eagerly loaded. */
    @Query("SELECT DISTINCT r FROM RoleModel r LEFT JOIN FETCH r.permissions WHERE r.institution.id = :institutionId")
    fun findAllByInstitutionIdWithPermissions(@Param("institutionId") institutionId: UUID): List<RoleModel>
}