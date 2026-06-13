package trustline.dashboard.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.DASHBOARD_METRICS
import trustline.dashboard.dto.DashboardOverviewDto
import trustline.dashboard.service.DashboardService
import java.time.LocalDateTime

@RestController
@RequestMapping("api/v1/admin/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/overview")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$DASHBOARD_METRICS')")
    fun overview(
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?,
        @RequestParam(value = "windowDays", required = false) windowDays: Int?,
        @RequestParam(value = "recentLimit", required = false, defaultValue = "5") recentLimit: Int
    ): DashboardOverviewDto = dashboardService.getOverview(from, to, windowDays, recentLimit)
}
