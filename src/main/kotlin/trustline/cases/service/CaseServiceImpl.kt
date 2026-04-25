package trustline.cases.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
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
import trustline.config.security.JWTConfigService
import trustline.institution.repository.UnitRepository
import trustline.institution.service.InstitutionService
import trustline.notification.service.NotificationService
import java.util.*

@Service
class CaseServiceImpl(
    private val caseRepository: CaseRepository,
    private val incidentTypeRepository: IncidentTypeRepository,
    private val caseFileUploadRepository: CaseFileUploadRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val commentRepository: CommentRepository,
    private val unitRepository: UnitRepository,
    private val fileUploadService: FileUploadService,
    private val jwtConfigService: JWTConfigService,
    private val institutionService: InstitutionService,
    private val userService: UserService,
    private val emailService: EmailService,
    private val notificationService: NotificationService
) : CaseService {

    @Transactional
    override fun createCase(request: CreateCaseDto, files: List<MultipartFile>?): CaseResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val user = userService.getUserById(authDetails.userId)

        val incidentType = incidentTypeRepository.findByIdAndInstitutionIdAndDeletedFalse(
            request.incidentTypeId!!, institution.id!!
        ) ?: throw NotFoundException("Incident type not found")

        val caseModel = caseRepository.save(
            CasesModel(
                incidentType = incidentType,
                dateOfIncident = request.dateOfIncident?.atStartOfDay(),
                institution = institution,
                location = request.location!!,
                description = request.description!!,
                user = user,
                caseStatus = Status.UNVERIFIED
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

    override fun getCaseById(id: UUID): CaseResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val caseModel = caseRepository.findByIdAndInstitutionIdAndIsDeletedFalse(id, authDetails.institutionId)
            ?: throw NotFoundException("Case not found")

        return toCaseResponse(caseModel, getFilesForCase(caseModel.id!!))
    }

    override fun getMyCases(): List<CaseResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        val cases = caseRepository.findByUserIdAndInstitutionIdAndIsDeletedFalse(
            authDetails.userId, authDetails.institutionId
        )
        return cases.map { toCaseResponse(it, getFilesForCase(it.id!!)) }
    }

    override fun getAllCases(): List<CaseResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        val cases = caseRepository.findByInstitutionIdAndIsDeletedFalse(authDetails.institutionId)
        return cases.map { toCaseResponse(it, getFilesForCase(it.id!!)) }
    }

    @Transactional
    override fun addComment(caseId: UUID, request: CreateCommentRequest): CommentResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
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
            message = "An update has been made on your case #${caseModel.id}.",
            user = caseModel.user
        )

        return toCommentResponse(comment)
    }

    @Transactional
    override fun closeCase(caseId: UUID): CaseResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val caseModel = caseRepository.findByIdAndInstitutionIdAndIsDeletedFalse(caseId, authDetails.institutionId)
            ?: throw NotFoundException("Case not found")

        if (caseModel.isClosed) throw BadRequestException("Case is already closed")

        caseModel.isClosed = true
        caseRepository.save(caseModel)
        return toCaseResponse(caseModel, getFilesForCase(caseModel.id!!))
    }

    @Transactional
    override fun reopenCase(caseId: UUID): CaseResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
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

    private fun toCommentResponse(comment: CommentsModel) = CommentResponseDto(
        id = comment.id!!,
        comment = comment.comment,
        commenterEmail = comment.commenter.email,
        createdAt = comment.createdAt
    )

    private fun toCaseResponse(caseModel: CasesModel, files: List<FileDto>) = CaseResponseDto(
        id = caseModel.id!!,
        incidentType = caseModel.incidentType.name,
        dateOfIncident = caseModel.dateOfIncident,
        location = caseModel.location,
        description = caseModel.description,
        reportedBy = caseModel.user.email,
        status = caseModel.caseStatus.name,
        closed = caseModel.isClosed,
        currentUnit = caseModel.currentUnit?.name,
        nextUnit = caseModel.nextUnit?.name,
        files = files,
        comments = getCommentsForCase(caseModel.id!!),
        createdAt = caseModel.createdAt,
        updatedAt = caseModel.updatedAt
    )
}
