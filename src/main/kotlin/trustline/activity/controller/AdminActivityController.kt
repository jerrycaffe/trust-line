package trustline.activity.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import trustline.activity.dto.*
import trustline.activity.service.ActivityService
import trustline.appuser.PagedResponse
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_ACTIVITIES
import java.util.*

@RestController
@RequestMapping("api/v1/admin/activities")
class AdminActivityController(
    private val activityService: ActivityService
) {
    @PostMapping
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun create(@Valid @RequestBody request: CreateActivityRequest): ActivityResponseDto =
        activityService.createActivity(request)

    @PutMapping("/{activityId}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun update(
        @PathVariable activityId: UUID,
        @Valid @RequestBody request: UpdateActivityRequest
    ): ActivityResponseDto = activityService.updateActivity(activityId, request)

    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun delete(@PathVariable activityId: UUID) = activityService.deleteActivity(activityId)

    @GetMapping("/{activityId}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun getOne(@PathVariable activityId: UUID): ActivityResponseDto =
        activityService.getActivityById(activityId)

    @GetMapping
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_ACTIVITIES')")
    fun list(
        @RequestParam(value = "offset", required = false, defaultValue = "0") offset: Int,
        @RequestParam(value = "limit", required = false, defaultValue = "20") limit: Int,
        @RequestParam(value = "search", required = false) search: String?
    ): PagedResponse<ActivityResponseDto> = activityService.listActivities(offset, limit, search)
}
