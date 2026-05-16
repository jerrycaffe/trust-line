package trustline.activity.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import trustline.activity.dto.*
import trustline.activity.service.ActivityMetricsService
import trustline.activity.service.UserActivityEntryService
import trustline.appuser.PagedResponse
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_ACTIVITIES
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("api/v1/admin/activities")
class AdminActivityMetricsController(
    private val metricsService: ActivityMetricsService,
    private val entryService: UserActivityEntryService
) {

    @GetMapping("/metrics/overview")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun platformOverview(
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?
    ): PlatformActivityOverviewDto = metricsService.platformOverview(from, to)

    @GetMapping("/{activityId}/metrics")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun activityMetrics(
        @PathVariable activityId: UUID,
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?
    ): ActivityMetricsDto = metricsService.activityMetrics(activityId, from, to)

    @GetMapping("/users/{userId}/metrics")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun userMetrics(
        @PathVariable userId: UUID,
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?
    ): UserMetricsSummaryDto = metricsService.userMetrics(userId, from, to)

    @GetMapping("/metrics/time-series")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun timeSeries(
        @RequestParam(value = "activityId", required = false) activityId: UUID?,
        @RequestParam(value = "userId", required = false) userId: UUID?,
        @RequestParam(value = "granularity", required = false, defaultValue = "day") granularity: String,
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?
    ): TimeSeriesResponseDto = metricsService.timeSeries(activityId, userId, granularity, from, to)

    @GetMapping("/entries")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun listEntries(
        @RequestParam(value = "activityId", required = false) activityId: UUID?,
        @RequestParam(value = "userId", required = false) userId: UUID?,
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?,
        @RequestParam(value = "offset", required = false, defaultValue = "0") offset: Int,
        @RequestParam(value = "limit", required = false, defaultValue = "20") limit: Int
    ): PagedResponse<ActivityEntryResponseDto> =
        entryService.listEntries(activityId, userId, from, to, offset, limit, adminScope = true)

    @GetMapping("/me/metrics")
    fun myMetrics(
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?
    ): UserMetricsSummaryDto = metricsService.myMetrics(from, to)
}
