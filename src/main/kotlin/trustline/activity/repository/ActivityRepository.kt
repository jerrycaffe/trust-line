package trustline.activity.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trustline.activity.model.ActivityModel
import java.util.*

@Repository
interface ActivityRepository : JpaRepository<ActivityModel, UUID> {

    fun findByIdAndInstitutionId(id: UUID, institutionId: UUID): ActivityModel?

    fun findByInstitutionId(institutionId: UUID, pageable: Pageable): Page<ActivityModel>

    @Query(
        """
        SELECT a FROM ActivityModel a
        WHERE a.institution.id = :institutionId
        AND (:searchPattern = '' OR LOWER(a.name) LIKE :searchPattern)
        """
    )
    fun searchByInstitution(
        @Param("institutionId") institutionId: UUID,
        @Param("searchPattern") searchPattern: String,
        pageable: Pageable
    ): Page<ActivityModel>

    fun existsByInstitutionIdAndNameIgnoreCase(institutionId: UUID, name: String): Boolean

    fun countByInstitutionId(institutionId: UUID): Long
}
