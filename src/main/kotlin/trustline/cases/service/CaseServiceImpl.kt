package trustline.cases.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import trustline.appuser.PageRequest
import trustline.appuser.PagedResponse
import trustline.appuser.Utility
import trustline.appuser.dto.EmailRequest
import trustline.appuser.dto.Status
import trustline.appuser.service.EmailService
import trustline.appuser.service.UserService
import trustline.cases.dto.*
import trustline.cases.model.CaseFileUploadsModel
import trustline.cases.model.CasesModel
import trustline.cases.model.CommentsModel
import trustline.cases.repository.*
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.AuthDetailsResponse
import trustline.institution.repository.UnitRepository
import trustline.institution.service.InstitutionService
import trustline.notification.service.NotificationService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class CaseServiceImpl(
    private val caseRepository: CaseRepository,
    private val incidentTypeRepository: IncidentTypeRepository,
    private val caseFileUploadRepository: CaseFileUploadRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val commentRepository: CommentRepository,
    private val unitRepository: UnitRepository,
    private val incidentTypeUnitsRepository: IncidentTypeUnitsRepository,
    private val fileUploadService: FileUploadService,
    private val institutionService: InstitutionService,
    private val userService: UserService,
    private val emailService: EmailService,
    private val notificationService: NotificationService
) : CaseService {

    @Transactional
    override fun createCase(authDetails: AuthDetailsResponse, request: CreateCaseDto, files: List<MultipartFile>?): CaseResponseDto {
        val institution = institutionService.getAuthUserInstitution()
        val user = userService.getUserById(authDetails.userId)

        val incidentType = incidentTypeRepository.findByIdAndInstitutionIdAndDeletedFalse(
            request.incidentTypeId!!, institution.id!!
        ) ?: throw NotFoundException("Incident type not found")

        val caseNumber = generateCaseNumber(institution.name!!, incidentType.name)

        val caseModel = caseRepository.save(
            CasesModel(
                incidentType = incidentType,
                dateOfIncident = request.dateOfIncident?.atStartOfDay() ?: LocalDate.now().atStartOfDay(),
                institution = institution,
                location = request.location!!,
                description = request.description!!,
                user = user,
                caseStatus = Status.PENDING,
                caseNumber = caseNumber
            )
        )

        val fileDtos = mutableListOf<FileDto>()
        if (!files.isNullOrEmpty()) {
            val fileIds = fileUploadService.uploadImages(files)
            val fileUploads = fileUploadRepository.findAllById(fileIds)

            fileUploads.forEach { fileUpload ->
                caseFileUploadRepository.save(
                    CaseFileUploadsModel(fileUpload = fileUpload, case = caseModel)
                )
                fileDtos.add(FileDto(id = fileUpload.id!!, url = fileUpload.uploadUrl))
            }
        }

        emailService.sendMail(
            EmailRequest(
                recipientName = user.email,
                recipientEmail = user.email,
                recipientId = user.id,
                htmlTemplate = Utility.caseAcknowledgementEmailTemplate(user.email, caseModel.id.toString()),
                subject = "Case Reported — Your Privacy is Protected"
            )
        )

        return toCaseResponse(caseModel, fileDtos)
    }

    override fun getCaseById(authDetails: AuthDetailsResponse, id: UUID): CaseResponseDto {
        val caseModel = caseRepository.findByIdAndInstitutionIdAndIsDeletedFalse(id, authDetails.institutionId)
            ?: throw NotFoundException("Case not found")

        return toCaseResponse(caseModel, getFilesForCase(caseModel.id!!))
    }

    override fun getMyCases(authDetails: AuthDetailsResponse, offset:Int, limit: Int): PagedResponse<CaseResponseDto> {
        val pageRequest = PageRequest(offset, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        val cases = caseRepository.findByUserIdAndInstitutionIdAndIsDeletedFalse(
            authDetails.userId, authDetails.institutionId, pageRequest
        )
        return PagedResponse(cases.map { toCaseResponse(it, getFilesForCase(it.id!!)) })
    }

    override fun getAllCases(
        authDetails: AuthDetailsResponse,
        offset: Int,
        limit: Int,
        status: Status?,
        incidentTypeId: UUID?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): PagedResponse<CaseResponseDto> {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw BadRequestException("endDate cannot be before startDate")
        }

        val effectiveStartDate = startDate?.atStartOfDay() ?: LocalDateTime.of(1970, 1, 1, 0, 0)
        val effectiveEndDate = when {
            endDate != null -> endDate.atTime(23, 59, 59, 999_999_999)
            startDate != null -> LocalDate.now().atTime(23, 59, 59, 999_999_999)
            else -> LocalDateTime.now()
        }

        val pageRequest = PageRequest(offset, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        val cases = caseRepository.findByInstitutionIdAndFiltersAndIsDeletedFalse(
            authDetails.institutionId,
            status,
            incidentTypeId,
            effectiveStartDate,
            effectiveEndDate,
            pageRequest
        )
        return PagedResponse(cases.map { toCaseResponse(it, getFilesForCase(it.id!!)) })
    }

    @Transactional
    override fun addComment(authDetails: AuthDetailsResponse, caseId: UUID, request: CreateCommentRequest): CommentResponseDto {
        val commenter = userService.getUserById(authDetails.userId)

        val caseModel = caseRepository.findByIdAndInstitutionIdAndIsDeletedFalse(caseId, authDetails.institutionId)
            ?: throw NotFoundException("Case not found")

        if (caseModel.isClosed) throw BadRequestException("Cannot comment on a closed case")

        val nextUnit = request.nextUnitId?.let {
            unitRepository.findById(it).orElseThrow { NotFoundException("Unit not found") }
        }

        val comment = commentRepository.save(
            CommentsModel(
                comment = request.comment!!,
                case = caseModel,
                commenter = commenter
            )
        )

        if (nextUnit != null) {
            caseModel.currentUnit = caseModel.nextUnit
            caseModel.nextUnit = nextUnit
            caseRepository.save(caseModel)
        }

        notificationService.createInternalNotification(
            topic = "New Comment on Your Case",
            message = "An update has been made on your case #${caseModel.caseNumber}.",
            user = caseModel.user
        )

        return toCommentResponse(comment)
    }

    @Transactional
    override fun concludeCase(authDetails: AuthDetailsResponse, caseId: UUID, request: ConcludeCaseRequest): CaseResponseDto {
        val commenter = userService.getUserById(authDetails.userId)

        val caseModel = caseRepository.findByIdAndInstitutionIdAndIsDeletedFalse(caseId, authDetails.institutionId)
            ?: throw NotFoundException("Case not found")

        if (caseModel.isClosed) throw BadRequestException("Case is already closed")

        commentRepository.save(
            CommentsModel(
                comment = request.concludeNote!!,
                case = caseModel,
                commenter = commenter
            )
        )

        caseModel.isClosed = true
        caseRepository.save(caseModel)

        notificationService.createInternalNotification(
            topic = "Case Concluded",
            message = "Your case #${caseModel.caseNumber} has been concluded.",
            user = caseModel.user
        )

        return toCaseResponse(caseModel, getFilesForCase(caseModel.id!!))
    }

    @Transactional
    override fun closeCase(authDetails: AuthDetailsResponse, caseId: UUID): CaseResponseDto {
        val caseModel = caseRepository.findByIdAndInstitutionIdAndIsDeletedFalse(caseId, authDetails.institutionId)
            ?: throw NotFoundException("Case not found")

        if (caseModel.isClosed) throw BadRequestException("Case is already closed")

        caseModel.isClosed = true
        caseRepository.save(caseModel)
        return toCaseResponse(caseModel, getFilesForCase(caseModel.id!!))
    }

    @Transactional
    override fun reopenCase(authDetails: AuthDetailsResponse, caseId: UUID): CaseResponseDto {
        val caseModel = caseRepository.findByIdAndInstitutionIdAndIsDeletedFalse(caseId, authDetails.institutionId)
            ?: throw NotFoundException("Case not found")

        if (!caseModel.isClosed) throw BadRequestException("Case is not closed")

        caseModel.isClosed = false
        caseRepository.save(caseModel)
        return toCaseResponse(caseModel, getFilesForCase(caseModel.id!!))
    }

    private fun getFilesForCase(caseId: UUID): List<FileDto> {
        return caseFileUploadRepository.findByCaseId(caseId).map {
            FileDto(id = it.fileUpload.id!!, url = it.fileUpload.uploadUrl)
        }
    }

    private fun getCommentsForCase(caseId: UUID): List<CommentResponseDto> {
        return commentRepository.findByCaseIdOrderByCreatedAtAsc(caseId).map { toCommentResponse(it) }
    }

    private fun generateCaseNumber(institutionName: String, incidentTypeName: String): String {
        val firstLetter = institutionName.firstOrNull()?.uppercaseChar() ?: 'X'
        val twoLetters = incidentTypeName.take(2).uppercase().padEnd(2, 'X')
        val fiveDigits = (10000..99999).random().toString()
        return "$firstLetter$twoLetters$fiveDigits"
    }

    private fun toCommentResponse(comment: CommentsModel) = CommentResponseDto(
        id = comment.id!!,
        comment = comment.comment,
        commenterEmail = comment.commenter.email,
        commenterUnit = comment.commenter.unit?.name,
        createdAt = comment.createdAt
    )

    private fun getAllUnitsInvolved(caseModel: CasesModel): List<String> {
        return incidentTypeUnitsRepository
            .findByIncidentTypeId(caseModel.incidentType.id!!)
            .mapNotNull { it.unit?.name }
            .distinct()
    }

    private fun getTreatedUnits(caseModel: CasesModel): List<String> {
        return listOfNotNull(caseModel.currentUnit?.name).distinct()
    }

    private fun calculateTracking(totalUnits: Int, treatedUnits: Int): Double {
        if (totalUnits == 0) return 0.0

        return (treatedUnits.toDouble() / totalUnits.toDouble()) * 100
    }

    private fun toCaseResponse(caseModel: CasesModel, files: List<FileDto>): CaseResponseDto {
        val allUnitsInvolved = getAllUnitsInvolved(caseModel)
        val comments = getCommentsForCase(caseModel.id!!)
        val unitsThatCommented = comments.mapNotNull { it.commenterUnit }.distinct()
        val unitsYetToComment = allUnitsInvolved.filterNot { unitsThatCommented.contains(it) }
        val treatedUnits = getTreatedUnits(caseModel)

        return CaseResponseDto(
        id = caseModel.id!!,
        caseNumber = caseModel.caseNumber,
        incidentType = caseModel.incidentType.name,
        dateOfIncident = caseModel.dateOfIncident,
        location = caseModel.location,
        description = caseModel.description,
        reportedBy = caseModel.user.email,
        status = caseModel.caseStatus.name,
        closed = caseModel.isClosed,
        currentUnit = caseModel.currentUnit?.name,
        nextUnit = caseModel.nextUnit?.name,
        allUnitsInvolved = allUnitsInvolved,
        unitsThatCommented = unitsThatCommented,
        unitsYetToComment = unitsYetToComment,
        treatedUnits = treatedUnits,
        tracking = calculateTracking(allUnitsInvolved.size, treatedUnits.size),
        files = files,
        comments = comments,
        createdAt = caseModel.createdAt,
        updatedAt = caseModel.updatedAt
    )
    }
}
