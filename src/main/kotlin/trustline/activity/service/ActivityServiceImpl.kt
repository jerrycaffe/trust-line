package trustline.activity.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import trustline.activity.dto.*
import trustline.activity.model.ActivityModel
import trustline.activity.model.GradeType
import trustline.activity.repository.ActivityRepository
import trustline.activity.repository.UserActivityEntryRepository
import trustline.appuser.PageRequest
import trustline.appuser.PagedResponse
import trustline.appuser.service.UserService
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.service.InstitutionService
import java.util.*

@Service
class ActivityServiceImpl(
    private val activityRepository: ActivityRepository,
    private val entryRepository: UserActivityEntryRepository,
    private val institutionService: InstitutionService,
    private val userService: UserService,
    private val jwtConfigService: JWTConfigService
) : ActivityService {

    @Transactional
    override fun createActivity(request: CreateActivityRequest): ActivityResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val user = userService.getUserById(authDetails.userId)

        val trimmedName = request.name.trim()
        if (trimmedName.isBlank()) throw BadRequestException("Activity name is required")
        if (activityRepository.existsByInstitutionIdAndNameIgnoreCase(institution.id!!, trimmedName)) {
            throw BadRequestException("An activity with this name already exists")
        }

        val saved = activityRepository.save(
            ActivityModel(
                institution = institution,
                name = trimmedName,
                description = request.description?.trim(),
                gradeType = request.gradeType,
                createdBy = user
            )
        )
        return saved.toResponse()
    }

    @Transactional
    override fun updateActivity(activityId: UUID, request: UpdateActivityRequest): ActivityResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val activity = activityRepository.findByIdAndInstitutionId(activityId, authDetails.institutionId)
            ?: throw NotFoundException("Activity not found")

        request.name?.let { newName ->
            val trimmed = newName.trim()
            if (trimmed.isBlank()) throw BadRequestException("Activity name cannot be empty")
            if (!trimmed.equals(activity.name, ignoreCase = true) &&
                activityRepository.existsByInstitutionIdAndNameIgnoreCase(authDetails.institutionId, trimmed)
            ) {
                throw BadRequestException("An activity with this name already exists")
            }
            activity.name = trimmed
        }
        if (request.description != null) activity.description = request.description.trim().ifBlank { null }
        if (request.gradeType != null) activity.gradeType = request.gradeType

        return activityRepository.save(activity).toResponse()
    }

    @Transactional
    override fun deleteActivity(activityId: UUID) {
        val authDetails = jwtConfigService.getAuthDetails()
        val activity = activityRepository.findByIdAndInstitutionId(activityId, authDetails.institutionId)
            ?: throw NotFoundException("Activity not found")
        activityRepository.delete(activity)
    }

    override fun getActivityById(activityId: UUID): ActivityResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val activity = activityRepository.findByIdAndInstitutionId(activityId, authDetails.institutionId)
            ?: throw NotFoundException("Activity not found")
        return activity.toResponse()
    }

    override fun listActivities(offset: Int, limit: Int, search: String?): PagedResponse<ActivityResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        val pageable = PageRequest(offset, limit, Sort.by("createdAt").descending())
        val trimmed = search?.trim().orEmpty()
        val searchPattern = if (trimmed.isEmpty()) "" else "%${trimmed.lowercase()}%"
        val page = activityRepository.searchByInstitution(
            authDetails.institutionId,
            searchPattern,
            pageable
        )
        return PagedResponse(page.map { it.toResponse() })
    }
}

internal fun validateValueForGradeType(gradeType: GradeType, value: java.math.BigDecimal) {
    when (gradeType) {
        GradeType.PERCENTAGE -> {
            if (value.signum() < 0 || value > java.math.BigDecimal("100")) {
                throw BadRequestException("Percentage value must be between 0 and 100")
            }
        }
        GradeType.TIME -> {
            if (value.signum() < 0) {
                throw BadRequestException("Time value (in seconds) must be zero or positive")
            }
        }
    }
}
