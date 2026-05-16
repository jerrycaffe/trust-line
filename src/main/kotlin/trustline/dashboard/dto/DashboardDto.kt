package trustline.dashboard.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class DashboardNewUserDto(
    val userId: UUID,
    val name: String,
    val email: String?,
    val phoneNumber: String?,
    val createdAt: LocalDateTime?
)

data class DashboardRecentCaseDto(
    val caseId: UUID,
    val caseNumber: String?,
    val incidentType: String,
    val status: String?,
    val reportedBy: String,
    val location: String,
    val createdAt: LocalDateTime?
)

data class DashboardActivityShareDto(
    val activityId: UUID,
    val name: String,
    val gradeType: String,
    val unit: String,
    val entryCount: Long,
    val percentage: BigDecimal
)

data class DashboardBreakdownItemDto(
    val key: String,
    val label: String,
    val count: Long,
    val percentage: BigDecimal
)

enum class TrendDirection { INCREASE, DECREASE, NO_CHANGE }

data class DashboardTrendDto(
    val currentCount: Long,
    val previousCount: Long,
    val change: Long,
    val percentageChange: BigDecimal,
    val direction: TrendDirection,
    val currentFrom: LocalDateTime,
    val currentTo: LocalDateTime,
    val previousFrom: LocalDateTime,
    val previousTo: LocalDateTime
)

data class DashboardPeriodDto(
    val from: LocalDateTime,
    val to: LocalDateTime
)

data class DashboardOverviewDto(
    val totalUsers: Long,
    val totalReports: Long,
    val period: DashboardPeriodDto,
    val newReports: Long,
    val newReportsTrend: DashboardTrendDto,
    val newUsersCount: Long,
    val newUsersTrend: DashboardTrendDto,
    val newUsers: List<DashboardNewUserDto>,
    val recentCases: List<DashboardRecentCaseDto>,
    val activities: List<DashboardActivityShareDto>,
    val casesByIncidentType: List<DashboardBreakdownItemDto>,
    val casesByStatus: List<DashboardBreakdownItemDto>
)
