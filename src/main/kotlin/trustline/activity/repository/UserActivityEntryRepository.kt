package trustline.activity.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trustline.activity.model.UserActivityEntryModel
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserActivityEntryRepository : JpaRepository<UserActivityEntryModel, UUID> {

    fun findByIdAndInstitutionId(id: UUID, institutionId: UUID): UserActivityEntryModel?

    @Query(
        """
        SELECT e FROM UserActivityEntryModel e
        WHERE e.institution.id = :institutionId
        AND e.activity.id = COALESCE(:activityId, e.activity.id)
        AND e.user.id = COALESCE(:userId, e.user.id)
        AND e.createdAt >= COALESCE(:from, e.createdAt)
        AND e.createdAt <= COALESCE(:to, e.createdAt)
        """
    )
    fun findFiltered(
        @Param("institutionId") institutionId: UUID,
        @Param("activityId") activityId: UUID?,
        @Param("userId") userId: UUID?,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?,
        pageable: Pageable
    ): Page<UserActivityEntryModel>

    // ---------- Aggregations ----------

    @Query(
        """
        SELECT
            COUNT(e) as totalEntries,
            COUNT(DISTINCT e.user.id) as uniqueUsers,
            AVG(e.value) as average,
            MIN(e.value) as minVal,
            MAX(e.value) as maxVal,
            SUM(e.value) as sumVal,
            MIN(e.createdAt) as firstEntryAt,
            MAX(e.createdAt) as lastEntryAt
        FROM UserActivityEntryModel e
        WHERE e.activity.id = :activityId
        AND e.createdAt >= COALESCE(:from, e.createdAt)
        AND e.createdAt <= COALESCE(:to, e.createdAt)
        """
    )
    fun aggregateForActivity(
        @Param("activityId") activityId: UUID,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?
    ): Array<Any?>

    @Query(
        """
        SELECT
            e.activity.id,
            COUNT(e),
            AVG(e.value),
            MIN(e.value),
            MAX(e.value),
            SUM(e.value),
            MIN(e.createdAt),
            MAX(e.createdAt)
        FROM UserActivityEntryModel e
        WHERE e.user.id = :userId
        AND e.institution.id = :institutionId
        AND e.createdAt >= COALESCE(:from, e.createdAt)
        AND e.createdAt <= COALESCE(:to, e.createdAt)
        GROUP BY e.activity.id
        """
    )
    fun aggregateForUserGroupedByActivity(
        @Param("userId") userId: UUID,
        @Param("institutionId") institutionId: UUID,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?
    ): List<Array<Any?>>

    @Query(
        """
        SELECT
            e.activity.id,
            COUNT(e),
            COUNT(DISTINCT e.user.id),
            AVG(e.value),
            MIN(e.value),
            MAX(e.value),
            SUM(e.value),
            MIN(e.createdAt),
            MAX(e.createdAt)
        FROM UserActivityEntryModel e
        WHERE e.institution.id = :institutionId
        AND e.createdAt >= COALESCE(:from, e.createdAt)
        AND e.createdAt <= COALESCE(:to, e.createdAt)
        GROUP BY e.activity.id
        """
    )
    fun aggregatePerActivityForInstitution(
        @Param("institutionId") institutionId: UUID,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?
    ): List<Array<Any?>>

    @Query(
        """
        SELECT COUNT(e)
        FROM UserActivityEntryModel e
        WHERE e.institution.id = :institutionId
        AND e.createdAt >= COALESCE(:from, e.createdAt)
        AND e.createdAt <= COALESCE(:to, e.createdAt)
        """
    )
    fun countPlatformEntries(
        @Param("institutionId") institutionId: UUID,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?
    ): Long

    @Query(
        """
        SELECT COUNT(DISTINCT e.user.id)
        FROM UserActivityEntryModel e
        WHERE e.institution.id = :institutionId
        AND e.createdAt >= COALESCE(:from, e.createdAt)
        AND e.createdAt <= COALESCE(:to, e.createdAt)
        """
    )
    fun countPlatformActiveUsers(
        @Param("institutionId") institutionId: UUID,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?
    ): Long

    @Query(
        value = """
        SELECT
            to_char(date_trunc(:granularity, e.created_at), 'YYYY-MM-DD HH24:MI') as bucket,
            COUNT(*) as cnt,
            AVG(e.value) as avg_val,
            SUM(e.value) as sum_val
        FROM user_activity_entries e
        WHERE e.institution_id = :institutionId
        AND (CAST(:activityId AS uuid) IS NULL OR e.activity_id = CAST(:activityId AS uuid))
        AND (CAST(:userId AS uuid) IS NULL OR e.user_id = CAST(:userId AS uuid))
        AND (CAST(:fromTs AS timestamp) IS NULL OR e.created_at >= CAST(:fromTs AS timestamp))
        AND (CAST(:toTs AS timestamp) IS NULL OR e.created_at <= CAST(:toTs AS timestamp))
        GROUP BY bucket
        ORDER BY bucket ASC
        """,
        nativeQuery = true
    )
    fun timeSeries(
        @Param("institutionId") institutionId: UUID,
        @Param("activityId") activityId: UUID?,
        @Param("userId") userId: UUID?,
        @Param("granularity") granularity: String,
        @Param("fromTs") fromTs: LocalDateTime?,
        @Param("toTs") toTs: LocalDateTime?
    ): List<Array<Any?>>

    @Query(
        """
        SELECT e FROM UserActivityEntryModel e
        WHERE e.user.id = :userId AND e.activity.id = :activityId
        ORDER BY e.createdAt DESC
        """
    )
    fun findLatestForUserAndActivity(
        @Param("userId") userId: UUID,
        @Param("activityId") activityId: UUID,
        pageable: Pageable
    ): List<UserActivityEntryModel>

    fun deleteByActivityId(activityId: UUID)

    @Query(
        """
        SELECT e.activity.id, COUNT(e)
        FROM UserActivityEntryModel e
        WHERE e.institution.id = :institutionId
        GROUP BY e.activity.id
        """
    )
    fun countGroupedByActivity(@Param("institutionId") institutionId: UUID): List<Array<Any?>>
}
