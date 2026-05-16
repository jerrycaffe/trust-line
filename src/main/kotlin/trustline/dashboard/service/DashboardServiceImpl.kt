package trustline.dashboard.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import trustline.activity.repository.ActivityRepository
import trustline.activity.repository.UserActivityEntryRepository
import trustline.appuser.PageRequest as CustomPageRequest
import trustline.appuser.repository.UserRepository
import trustline.cases.repository.CaseRepository
import trustline.config.security.JWTConfigService
import trustline.dashboard.dto.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class DashboardServiceImpl(
    private val userRepository: UserRepository,
    private val caseRepository: CaseRepository,
    private val activityRepository: ActivityRepository,
    private val entryRepository: UserActivityEntryRepository,
    private val jwtConfigService: JWTConfigService
) : DashboardService {

    override fun getOverview(
        from: LocalDateTime?,
        to: LocalDateTime?,
        windowDays: Int?,
        recentLimit: Int
    ): DashboardOverviewDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val institutionId = authDetails.institutionId
        val limit = if (recentLimit <= 0) 5 else recentLimit

        val now = LocalDateTime.now()
        val resolvedTo = to ?: now
        val resolvedFrom = from ?: resolvedTo.minusDays(((windowDays ?: 7).coerceAtLeast(1)).toLong())
        require(!resolvedFrom.isAfter(resolvedTo)) { "from must be before or equal to to" }

        val periodDuration = Duration.between(resolvedFrom, resolvedTo)
        val previousTo = resolvedFrom
        val previousFrom = resolvedFrom.minus(periodDuration)

        val totalUsers = userRepository.countNonAdminUsersByInstitutionId(institutionId)
        val totalReports = caseRepository.countByInstitution(institutionId)

        val currentNewUsers = userRepository.countNewNonAdminUsersBetween(institutionId, resolvedFrom, resolvedTo)
        val previousNewUsers = userRepository.countNewNonAdminUsersBetween(institutionId, previousFrom, previousTo)

        val currentNewReports = caseRepository.countByInstitutionBetween(institutionId, resolvedFrom, resolvedTo)
        val previousNewReports = caseRepository.countByInstitutionBetween(institutionId, previousFrom, previousTo)

        val newUsersTrend = buildTrend(currentNewUsers, previousNewUsers, resolvedFrom, resolvedTo, previousFrom, previousTo)
        val newReportsTrend = buildTrend(currentNewReports, previousNewReports, resolvedFrom, resolvedTo, previousFrom, previousTo)

        val newUsers = userRepository
            .findRecentNonAdminUsers(institutionId, PageRequest.of(0, limit))
            .map { u ->
                val name = listOfNotNull(u.firstName, u.lastName)
                    .joinToString(" ").ifBlank { u.email ?: "" }
                DashboardNewUserDto(
                    userId = u.id!!,
                    name = name,
                    email = u.email,
                    phoneNumber = u.phoneNumber,
                    createdAt = u.createdAt
                )
            }

        val recentCasesPage = caseRepository.findByInstitutionIdAndIsDeletedFalse(
            institutionId,
            CustomPageRequest(0, limit, Sort.by("createdAt").descending())
        )
        val recentCases = recentCasesPage.content.map { c ->
            val reporter = listOfNotNull(c.user.firstName, c.user.lastName)
                .joinToString(" ").ifBlank { c.user.email ?: "" }
            DashboardRecentCaseDto(
                caseId = c.id!!,
                caseNumber = c.caseNumber,
                incidentType = c.incidentType.name,
                status = c.caseStatus.name,
                reportedBy = reporter,
                location = c.location,
                createdAt = c.createdAt
            )
        }

        val activities = activityRepository
            .searchByInstitution(institutionId, "", PageRequest.of(0, 1000, Sort.by("name").ascending()))
            .content
        val entryCounts = entryRepository.countGroupedByActivity(institutionId)
            .mapNotNull { row ->
                val id = row[0] as? UUID ?: return@mapNotNull null
                id to ((row[1] as? Number)?.toLong() ?: 0L)
            }.toMap()
        val totalEntries = entryCounts.values.sum()
        val activityShares = activities.map { a ->
            val count = entryCounts[a.id] ?: 0L
            DashboardActivityShareDto(
                activityId = a.id!!,
                name = a.name,
                gradeType = a.gradeType.name,
                unit = when (a.gradeType.name) { "TIME" -> "seconds"; else -> "%" },
                entryCount = count,
                percentage = percentage(count, totalEntries)
            )
        }

        val incidentRows = caseRepository.countGroupedByIncidentType(institutionId)
        val incidentTotal = incidentRows.sumOf { (it[2] as? Number)?.toLong() ?: 0L }
        val casesByIncidentType = incidentRows.mapNotNull { row ->
            val id = row[0] as? UUID ?: return@mapNotNull null
            val name = (row[1] as? String) ?: "Unknown"
            val count = (row[2] as? Number)?.toLong() ?: 0L
            DashboardBreakdownItemDto(
                key = id.toString(),
                label = name,
                count = count,
                percentage = percentage(count, incidentTotal)
            )
        }.sortedByDescending { it.count }

        val statusRows = caseRepository.countGroupedByStatus(institutionId)
        val statusTotal = statusRows.sumOf { (it[1] as? Number)?.toLong() ?: 0L }
        val casesByStatus = statusRows.map { row ->
            val status = row[0]?.toString() ?: "UNKNOWN"
            val count = (row[1] as? Number)?.toLong() ?: 0L
            DashboardBreakdownItemDto(
                key = status,
                label = status,
                count = count,
                percentage = percentage(count, statusTotal)
            )
        }.sortedByDescending { it.count }

        return DashboardOverviewDto(
            totalUsers = totalUsers,
            totalReports = totalReports,
            period = DashboardPeriodDto(resolvedFrom, resolvedTo),
            newReports = currentNewReports,
            newReportsTrend = newReportsTrend,
            newUsersCount = currentNewUsers,
            newUsersTrend = newUsersTrend,
            newUsers = newUsers,
            recentCases = recentCases,
            activities = activityShares,
            casesByIncidentType = casesByIncidentType,
            casesByStatus = casesByStatus
        )
    }

    private fun buildTrend(
        current: Long,
        previous: Long,
        currentFrom: LocalDateTime,
        currentTo: LocalDateTime,
        previousFrom: LocalDateTime,
        previousTo: LocalDateTime
    ): DashboardTrendDto {
        val change = current - previous
        val direction = when {
            change > 0 -> TrendDirection.INCREASE
            change < 0 -> TrendDirection.DECREASE
            else -> TrendDirection.NO_CHANGE
        }
        val pct = when {
            previous == 0L && current == 0L -> BigDecimal.ZERO.setScale(2)
            previous == 0L -> BigDecimal(100).setScale(2)
            else -> BigDecimal(kotlin.math.abs(change))
                .multiply(BigDecimal(100))
                .divide(BigDecimal(previous), 2, RoundingMode.HALF_UP)
        }
        return DashboardTrendDto(
            currentCount = current,
            previousCount = previous,
            change = change,
            percentageChange = pct,
            direction = direction,
            currentFrom = currentFrom,
            currentTo = currentTo,
            previousFrom = previousFrom,
            previousTo = previousTo
        )
    }

    private fun percentage(count: Long, total: Long): BigDecimal {
        if (total <= 0L) return BigDecimal.ZERO.setScale(2)
        return BigDecimal(count)
            .multiply(BigDecimal(100))
            .divide(BigDecimal(total), 2, RoundingMode.HALF_UP)
    }
}
