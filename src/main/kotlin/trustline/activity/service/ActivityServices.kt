package trustline.activity.service

import trustline.activity.dto.*
import org.springframework.data.domain.Pageable
import trustline.appuser.PagedResponse
import java.time.LocalDateTime
import java.util.*

interface ActivityService {
    // Admin
    fun createActivity(request: CreateActivityRequest): ActivityResponseDto
    fun updateActivity(activityId: UUID, request: UpdateActivityRequest): ActivityResponseDto
    fun deleteActivity(activityId: UUID)

    // Shared
    fun getActivityById(activityId: UUID): ActivityResponseDto
    fun listActivities(offset: Int, limit: Int, search: String?): PagedResponse<ActivityResponseDto>
}

interface UserActivityEntryService {
    fun submitEntry(activityId: UUID, request: SubmitEntryRequest): ActivityEntryResponseDto
    fun updateEntry(entryId: UUID, request: UpdateEntryRequest): ActivityEntryResponseDto
    fun deleteEntry(entryId: UUID)
    fun getEntry(entryId: UUID): ActivityEntryResponseDto

    fun listEntries(
        activityId: UUID?,
        userId: UUID?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        offset: Int,
        limit: Int,
        adminScope: Boolean
    ): PagedResponse<ActivityEntryResponseDto>
}

interface ActivityMetricsService {
    fun activityMetrics(activityId: UUID, from: LocalDateTime?, to: LocalDateTime?): ActivityMetricsDto
    fun userMetrics(userId: UUID, from: LocalDateTime?, to: LocalDateTime?): UserMetricsSummaryDto
    fun myMetrics(from: LocalDateTime?, to: LocalDateTime?): UserMetricsSummaryDto
    fun platformOverview(from: LocalDateTime?, to: LocalDateTime?): PlatformActivityOverviewDto
    fun timeSeries(
        activityId: UUID?,
        userId: UUID?,
        granularity: String,
        from: LocalDateTime?,
        to: LocalDateTime?
    ): TimeSeriesResponseDto
}
