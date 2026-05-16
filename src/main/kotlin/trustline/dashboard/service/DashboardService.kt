package trustline.dashboard.service

import trustline.dashboard.dto.DashboardOverviewDto
import java.time.LocalDateTime

interface DashboardService {
    fun getOverview(
        from: LocalDateTime?,
        to: LocalDateTime?,
        windowDays: Int?,
        recentLimit: Int
    ): DashboardOverviewDto
}
