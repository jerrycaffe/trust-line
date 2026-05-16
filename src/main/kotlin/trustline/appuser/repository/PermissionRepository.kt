package trustline.appuser.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trustline.appuser.model.PermissionModel
import java.util.*

/**
 * Spring Data JPA repository for [PermissionModel] entities.
 *
 * Scoping helpers return a combined view of institution-specific and global
 * permissions so that administrators can assign system-wide permissions
 * (seeded by Flyway) to custom institution roles.
 */
@Repository
interface PermissionRepository : JpaRepository<PermissionModel, UUID> {
    /** Any-institution lookup, primarily used for duplicate-name checks. */
    fun findByName(name: String): PermissionModel?
    /** Finds a permission by name strictly within a specific institution. */
    fun findByNameAndInstitutionId(name: String, institutionId: UUID): PermissionModel?

    /** Finds a global permission (institution IS NULL) by name. */
    fun findByNameAndInstitutionIdIsNull(name: String): PermissionModel?

    /** Returns institution-specific permissions plus global (no-institution) permissions. */
    /**
     * Returns all permissions visible to [institutionId]: those belonging to the
     * institution itself **plus** any globally-defined permissions (institution IS NULL).
     */
    @Query("SELECT p FROM PermissionModel p WHERE p.institution.id = :institutionId OR p.institution IS NULL")
    fun findAllByInstitutionIdOrGlobal(@Param("institutionId") institutionId: UUID): List<PermissionModel>
}
