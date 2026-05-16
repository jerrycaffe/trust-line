package trustline.cases.repository

import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trustline.appuser.PageRequest
import trustline.appuser.dto.Status
import trustline.cases.model.CasesModel
import java.time.LocalDateTime
import java.util.*

@Repository
interface CaseRepository : JpaRepository<CasesModel, UUID> {
    fun findByUserIdAndInstitutionIdAndIsDeletedFalse(userId: UUID, institutionId: UUID, pageRequet: PageRequest): Page<CasesModel>

    @Query(
        """
        SELECT c
        FROM CasesModel c
        WHERE c.institution.id = :institutionId
          AND c.isDeleted = false
          AND (:status IS NULL OR c.caseStatus = :status)
          AND (:incidentTypeId IS NULL OR c.incidentType.id = :incidentTypeId)
          AND (c.createdAt IS NULL OR (c.createdAt >= :startDate AND c.createdAt <= :endDate))
        """
    )
    fun findByInstitutionIdAndFiltersAndIsDeletedFalse(
        @Param("institutionId") institutionId: UUID,
        @Param("status") status: Status?,
        @Param("incidentTypeId") incidentTypeId: UUID?,
                @Param("startDate") startDate: LocalDateTime,
                @Param("endDate") endDate: LocalDateTime,
        pageRequest: PageRequest
    ): Page<CasesModel>

    fun findByInstitutionIdAndIsDeletedFalse(institutionId: UUID, pageRequest: PageRequest): Page<CasesModel>
    fun findByInstitutionIdAndIsDeletedFalse(institutionId: UUID): List<CasesModel>
    fun findByIdAndInstitutionIdAndIsDeletedFalse(id: UUID, institutionId: UUID): CasesModel?
    fun countByUserIdAndInstitutionIdAndIsDeletedFalseAndIsClosedFalse(userId: UUID, institutionId: UUID): Long

    @Query(
        """
        SELECT COUNT(c) FROM CasesModel c
        WHERE c.institution.id = :institutionId AND c.isDeleted = false
        """
    )
    fun countByInstitution(@Param("institutionId") institutionId: UUID): Long

    @Query(
        """
        SELECT COUNT(c) FROM CasesModel c
        WHERE c.institution.id = :institutionId AND c.isDeleted = false
          AND c.createdAt >= :since
        """
    )
    fun countByInstitutionSince(
        @Param("institutionId") institutionId: UUID,
        @Param("since") since: LocalDateTime
    ): Long

    @Query(
        """
        SELECT COUNT(c) FROM CasesModel c
        WHERE c.institution.id = :institutionId AND c.isDeleted = false
          AND c.createdAt >= :from AND c.createdAt < :to
        """
    )
    fun countByInstitutionBetween(
        @Param("institutionId") institutionId: UUID,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): Long

    @Query(
        """
        SELECT c.incidentType.id, c.incidentType.name, COUNT(c)
        FROM CasesModel c
        WHERE c.institution.id = :institutionId AND c.isDeleted = false
        GROUP BY c.incidentType.id, c.incidentType.name
        """
    )
    fun countGroupedByIncidentType(@Param("institutionId") institutionId: UUID): List<Array<Any?>>

    @Query(
        """
        SELECT c.caseStatus, COUNT(c)
        FROM CasesModel c
        WHERE c.institution.id = :institutionId AND c.isDeleted = false
        GROUP BY c.caseStatus
        """
    )
    fun countGroupedByStatus(@Param("institutionId") institutionId: UUID): List<Array<Any?>>
}
