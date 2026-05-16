package trustline.activity.service

import org.springframework.stereotype.Service
import trustline.activity.dto.*
import trustline.activity.model.GradeType
import trustline.activity.repository.ActivityRepository
import trustline.activity.repository.UserActivityEntryRepository
import trustline.appuser.service.UserService
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class ActivityMetricsServiceImpl(
    private val activityRepository: ActivityRepository,
    private val entryRepository: UserActivityEntryRepository,
    private val userService: UserService,
    private val jwtConfigService: JWTConfigService
) : ActivityMetricsService {

    override fun activityMetrics(activityId: UUID, from: LocalDateTime?, to: LocalDateTime?): ActivityMetricsDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val activity = activityRepository.findByIdAndInstitutionId(activityId, authDetails.institutionId)
            ?: throw NotFoundException("Activity not found")

        val row = entryRepository.aggregateForActivity(activityId, from, to)
        return ActivityMetricsDto(
            activityId = activity.id!!,
            activityName = activity.name,
            gradeType = activity.gradeType,
            unit = activity.gradeType.unitLabel(),
            totalEntries = row[0].asLong(),
            uniqueUsers = row[1].asLong(),
            average = row[2].asBigDecimal(),
            min = row[3].asBigDecimal(),
            max = row[4].asBigDecimal(),
            sum = row[5].asBigDecimal(),
            firstEntryAt = row[6].asLocalDateTime(),
            lastEntryAt = row[7].asLocalDateTime()
        )
    }

    override fun userMetrics(userId: UUID, from: LocalDateTime?, to: LocalDateTime?): UserMetricsSummaryDto {
        return buildUserMetrics(userId, from, to)
    }

    override fun myMetrics(from: LocalDateTime?, to: LocalDateTime?): UserMetricsSummaryDto {
        val authDetails = jwtConfigService.getAuthDetails()
        return buildUserMetrics(authDetails.userId, from, to)
    }

    private fun buildUserMetrics(userId: UUID, from: LocalDateTime?, to: LocalDateTime?): UserMetricsSummaryDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val user = userService.getUserById(userId)

        val rows = entryRepository.aggregateForUserGroupedByActivity(userId, authDetails.institutionId, from, to)
        val activities = activityRepository.findAllById(rows.mapNotNull { it[0] as? UUID })
            .associateBy { it.id }

        val per = rows.mapNotNull { r ->
            val aId = r[0] as? UUID ?: return@mapNotNull null
            val activity = activities[aId] ?: return@mapNotNull null
            val activityId = activity.id!!
            val latest = entryRepository.findLatestForUserAndActivity(
                userId,
                activityId,
                org.springframework.data.domain.PageRequest.of(0, 1)
            ).firstOrNull()
            UserActivityMetricsDto(
                activityId = activityId,
                activityName = activity.name,
                gradeType = activity.gradeType,
                unit = activity.gradeType.unitLabel(),
                entryCount = r[1].asLong(),
                average = r[2].asBigDecimal(),
                min = r[3].asBigDecimal(),
                max = r[4].asBigDecimal(),
                sum = r[5].asBigDecimal(),
                latestValue = latest?.value,
                firstEntryAt = r[6].asLocalDateTime(),
                lastEntryAt = r[7].asLocalDateTime()
            )
        }

        val totalEntries = per.sumOf { it.entryCount }
        val userName = listOfNotNull(user.firstName, user.lastName)
            .joinToString(" ").ifBlank { user.email ?: "" }

        return UserMetricsSummaryDto(
            userId = user.id!!,
            userName = userName,
            totalEntries = totalEntries,
            activitiesParticipated = per.size.toLong(),
            perActivity = per
        )
    }

    override fun platformOverview(from: LocalDateTime?, to: LocalDateTime?): PlatformActivityOverviewDto {
        val authDetails = jwtConfigService.getAuthDetails()

        val totalEntries = entryRepository.countPlatformEntries(authDetails.institutionId, from, to)
        val activeUsers = entryRepository.countPlatformActiveUsers(authDetails.institutionId, from, to)

        val rows = entryRepository.aggregatePerActivityForInstitution(authDetails.institutionId, from, to)
        val activitiesById = activityRepository.findAllById(rows.mapNotNull { it[0] as? UUID })
            .associateBy { it.id }

        val perActivity = rows.mapNotNull { r ->
            val aId = r[0] as? UUID ?: return@mapNotNull null
            val activity = activitiesById[aId] ?: return@mapNotNull null
            ActivityMetricsDto(
                activityId = activity.id!!,
                activityName = activity.name,
                gradeType = activity.gradeType,
                unit = activity.gradeType.unitLabel(),
                totalEntries = r[1].asLong(),
                uniqueUsers = r[2].asLong(),
                average = r[3].asBigDecimal(),
                min = r[4].asBigDecimal(),
                max = r[5].asBigDecimal(),
                sum = r[6].asBigDecimal(),
                firstEntryAt = r[7].asLocalDateTime(),
                lastEntryAt = r[8].asLocalDateTime()
            )
        }

        return PlatformActivityOverviewDto(
            totalActivities = activityRepository.countByInstitutionId(authDetails.institutionId),
            totalEntries = totalEntries,
            activeUsers = activeUsers,
            perActivity = perActivity
        )
    }

    override fun timeSeries(
        activityId: UUID?,
        userId: UUID?,
        granularity: String,
        from: LocalDateTime?,
        to: LocalDateTime?
    ): TimeSeriesResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val g = granularity.lowercase()
        if (g !in setOf("hour", "day", "week", "month", "quarter", "year")) {
            throw BadRequestException("granularity must be one of: hour, day, week, month, quarter, year")
        }
        if (activityId != null) {
            activityRepository.findByIdAndInstitutionId(activityId, authDetails.institutionId)
                ?: throw NotFoundException("Activity not found")
        }
        val rows = entryRepository.timeSeries(
            authDetails.institutionId,
            activityId,
            userId,
            g,
            from,
            to
        )
        val points = rows.map { r ->
            TimeSeriesPointDto(
                bucket = (r[0] as? String) ?: "",
                entryCount = r[1].asLong(),
                average = r[2].asBigDecimal(),
                sum = r[3].asBigDecimal()
            )
        }
        return TimeSeriesResponseDto(
            activityId = activityId,
            userId = userId,
            granularity = g,
            from = from,
            to = to,
            points = points
        )
    }
}
