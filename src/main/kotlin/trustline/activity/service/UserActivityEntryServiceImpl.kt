package trustline.activity.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import trustline.activity.dto.*
import trustline.activity.model.UserActivityEntryModel
import trustline.activity.repository.ActivityRepository
import trustline.activity.repository.UserActivityEntryRepository
import trustline.appuser.PageRequest
import trustline.appuser.PagedResponse
import trustline.appuser.service.UserService
import org.springframework.security.access.AccessDeniedException
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.service.InstitutionService
import java.time.LocalDateTime
import java.util.*

@Service
class UserActivityEntryServiceImpl(
    private val entryRepository: UserActivityEntryRepository,
    private val activityRepository: ActivityRepository,
    private val institutionService: InstitutionService,
    private val userService: UserService,
    private val jwtConfigService: JWTConfigService
) : UserActivityEntryService {

    @Transactional
    override fun submitEntry(activityId: UUID, request: SubmitEntryRequest): ActivityEntryResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val user = userService.getUserById(authDetails.userId)

        val activity = activityRepository.findByIdAndInstitutionId(activityId, institution.id!!)
            ?: throw NotFoundException("Activity not found")

        validateValueForGradeType(activity.gradeType, request.value)

        val saved = entryRepository.save(
            UserActivityEntryModel(
                activity = activity,
                user = user,
                institution = institution,
                value = request.value,
                notes = request.notes?.trim()?.ifBlank { null }
            )
        )
        return saved.toResponse()
    }

    @Transactional
    override fun updateEntry(entryId: UUID, request: UpdateEntryRequest): ActivityEntryResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val entry = entryRepository.findByIdAndInstitutionId(entryId, authDetails.institutionId)
            ?: throw NotFoundException("Entry not found")
        if (entry.user.id != authDetails.userId) {
            throw AccessDeniedException("You can only update your own entries")
        }
        if (request.value != null) {
            validateValueForGradeType(entry.activity.gradeType, request.value)
            entry.value = request.value
        }
        if (request.notes != null) entry.notes = request.notes.trim().ifBlank { null }
        return entryRepository.save(entry).toResponse()
    }

    @Transactional
    override fun deleteEntry(entryId: UUID) {
        val authDetails = jwtConfigService.getAuthDetails()
        val entry = entryRepository.findByIdAndInstitutionId(entryId, authDetails.institutionId)
            ?: throw NotFoundException("Entry not found")
        if (entry.user.id != authDetails.userId) {
            throw AccessDeniedException("You can only delete your own entries")
        }
        entryRepository.delete(entry)
    }

    override fun getEntry(entryId: UUID): ActivityEntryResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val entry = entryRepository.findByIdAndInstitutionId(entryId, authDetails.institutionId)
            ?: throw NotFoundException("Entry not found")
        return entry.toResponse()
    }

    override fun listEntries(
        activityId: UUID?,
        userId: UUID?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        offset: Int,
        limit: Int,
        adminScope: Boolean
    ): PagedResponse<ActivityEntryResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        val effectiveUserId = if (adminScope) userId else authDetails.userId
        val pageable = PageRequest(offset, limit, Sort.by("createdAt").descending())
        val page = entryRepository.findFiltered(
            authDetails.institutionId,
            activityId,
            effectiveUserId,
            from,
            to,
            pageable
        )
        return PagedResponse(page.map { it.toResponse() })
    }
}
