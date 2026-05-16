package trustline.activity.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import trustline.activity.model.GradeType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class CreateActivityRequest(
    @field:NotBlank(message = "Activity name is required")
    val name: String,
    val description: String? = null,
    @field:NotNull(message = "Grade type is required (PERCENTAGE or TIME)")
    val gradeType: GradeType
)

data class UpdateActivityRequest(
    val name: String? = null,
    val description: String? = null,
    val gradeType: GradeType? = null
)

data class ActivityResponseDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val gradeType: GradeType,
    val unit: String,
    val createdById: UUID,
    val createdByName: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class SubmitEntryRequest(
    @field:NotNull(message = "Value is required")
    val value: BigDecimal,
    val notes: String? = null
)

data class UpdateEntryRequest(
    val value: BigDecimal? = null,
    val notes: String? = null
)

data class ActivityEntryResponseDto(
    val id: UUID,
    val activityId: UUID,
    val activityName: String,
    val gradeType: GradeType,
    val unit: String,
    val userId: UUID,
    val userName: String,
    val value: BigDecimal,
    val notes: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class ActivityMetricsDto(
    val activityId: UUID,
    val activityName: String,
    val gradeType: GradeType,
    val unit: String,
    val totalEntries: Long,
    val uniqueUsers: Long,
    val average: BigDecimal?,
    val min: BigDecimal?,
    val max: BigDecimal?,
    val sum: BigDecimal?,
    val firstEntryAt: LocalDateTime?,
    val lastEntryAt: LocalDateTime?
)

data class UserActivityMetricsDto(
    val activityId: UUID,
    val activityName: String,
    val gradeType: GradeType,
    val unit: String,
    val entryCount: Long,
    val average: BigDecimal?,
    val min: BigDecimal?,
    val max: BigDecimal?,
    val sum: BigDecimal?,
    val latestValue: BigDecimal?,
    val firstEntryAt: LocalDateTime?,
    val lastEntryAt: LocalDateTime?
)

data class UserMetricsSummaryDto(
    val userId: UUID,
    val userName: String,
    val totalEntries: Long,
    val activitiesParticipated: Long,
    val perActivity: List<UserActivityMetricsDto>
)

data class PlatformActivityOverviewDto(
    val totalActivities: Long,
    val totalEntries: Long,
    val activeUsers: Long,
    val perActivity: List<ActivityMetricsDto>
)

data class TimeSeriesPointDto(
    val bucket: String,
    val entryCount: Long,
    val average: BigDecimal?,
    val sum: BigDecimal?
)

data class TimeSeriesResponseDto(
    val activityId: UUID?,
    val userId: UUID?,
    val granularity: String,
    val from: LocalDateTime?,
    val to: LocalDateTime?,
    val points: List<TimeSeriesPointDto>
)
