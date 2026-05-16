package trustline.activity.controller

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import trustline.activity.dto.*
import trustline.activity.service.ActivityService
import trustline.activity.service.UserActivityEntryService
import trustline.appuser.PagedResponse
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("api/v1/activities")
class ActivityController(
    private val activityService: ActivityService,
    private val entryService: UserActivityEntryService
) {

    @GetMapping
    fun list(
        @RequestParam(value = "offset", required = false, defaultValue = "0") offset: Int,
        @RequestParam(value = "limit", required = false, defaultValue = "20") limit: Int,
        @RequestParam(value = "search", required = false) search: String?
    ): PagedResponse<ActivityResponseDto> = activityService.listActivities(offset, limit, search)

    @GetMapping("/{activityId}")
    fun getOne(@PathVariable activityId: UUID): ActivityResponseDto =
        activityService.getActivityById(activityId)

    @PostMapping("/{activityId}/entries")
    fun submitEntry(
        @PathVariable activityId: UUID,
        @Valid @RequestBody request: SubmitEntryRequest
    ): ActivityEntryResponseDto = entryService.submitEntry(activityId, request)

    @PutMapping("/entries/{entryId}")
    fun updateEntry(
        @PathVariable entryId: UUID,
        @Valid @RequestBody request: UpdateEntryRequest
    ): ActivityEntryResponseDto = entryService.updateEntry(entryId, request)

    @DeleteMapping("/entries/{entryId}")
    fun deleteEntry(@PathVariable entryId: UUID) = entryService.deleteEntry(entryId)

    @GetMapping("/entries/{entryId}")
    fun getEntry(@PathVariable entryId: UUID): ActivityEntryResponseDto =
        entryService.getEntry(entryId)

    @GetMapping("/me/entries")
    fun myEntries(
        @RequestParam(value = "activityId", required = false) activityId: UUID?,
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?,
        @RequestParam(value = "offset", required = false, defaultValue = "0") offset: Int,
        @RequestParam(value = "limit", required = false, defaultValue = "20") limit: Int
    ): PagedResponse<ActivityEntryResponseDto> =
        entryService.listEntries(activityId, null, from, to, offset, limit, adminScope = false)
}
