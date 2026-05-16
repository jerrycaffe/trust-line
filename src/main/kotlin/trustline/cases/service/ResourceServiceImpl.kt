package trustline.cases.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import trustline.appuser.PageRequest
import trustline.appuser.PagedResponse
import trustline.appuser.service.UserService
import trustline.cases.dto.CreateResourceRequest
import trustline.cases.dto.ResourceResponseDto
import trustline.cases.dto.UpdateResourceRequest
import trustline.cases.model.FileUploadsModel
import trustline.cases.model.ResourceModel
import trustline.cases.repository.FileUploadRepository
import trustline.cases.repository.IncidentTypeRepository
import trustline.cases.repository.ResourceRepository
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.service.InstitutionService
import java.util.*

@Service
class ResourceServiceImpl(
    private val resourceRepository: ResourceRepository,
    private val incidentTypeRepository: IncidentTypeRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val institutionService: InstitutionService,
    private val userService: UserService,
    private val jwtConfigService: JWTConfigService,
    private val cloudinary: Cloudinary
) : ResourceService {

    @Transactional
    override fun createResource(request: CreateResourceRequest, file: MultipartFile?): ResourceResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val user = userService.getUserById(authDetails.userId)

        val incidentType = incidentTypeRepository.findByIdAndInstitutionIdAndDeletedFalse(
            request.incidentTypeId!!, institution.id!!
        ) ?: throw NotFoundException("Incident type not found")

        val fileUpload = file?.let { uploadFile(it) }

        val resource = resourceRepository.save(
            ResourceModel(
                name = request.name!!,
                contents = request.contents,
                incidentType = incidentType,
                fileUpload = fileUpload,
                createdBy = user,
                institution = institution
            )
        )
        return toResponse(resource)
    }

    @Transactional
    override fun updateResource(resourceId: UUID, request: UpdateResourceRequest, file: MultipartFile?): ResourceResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val resource = getResourceModel(resourceId)

        if (request.name != null) resource.name = request.name
        if (request.incidentTypeId != null) {
            val incidentType = incidentTypeRepository.findByIdAndInstitutionIdAndDeletedFalse(
                request.incidentTypeId,
                authDetails.institutionId
            ) ?: throw NotFoundException("Incident type not found")
            resource.incidentType = incidentType
        }
        if (request.contents != null) resource.contents = request.contents
        if (file != null) resource.fileUpload = uploadFile(file)

        resourceRepository.save(resource)
        return toResponse(resource)
    }

    @Transactional
    override fun deleteResource(resourceId: UUID) {
        val resource = getResourceModel(resourceId)
        resourceRepository.delete(resource)
    }

    override fun getResourceById(resourceId: UUID): ResourceResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val resource = resourceRepository.findByIdAndInstitutionIdAndDeletedFalse(resourceId, authDetails.institutionId)
            ?: throw NotFoundException("Resource not found")
        return toResponse(resource)
    }

    override fun getAllResources(offset: Int, limit: Int): PagedResponse<ResourceResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        val page = resourceRepository.findByInstitutionIdAndDeletedFalse(
            authDetails.institutionId,
            PageRequest(offset, limit, Sort.by("createdAt").descending())
        )
        return PagedResponse(page.map { toResponse(it) })
    }

    override fun getResourcesByIncidentType(incidentTypeId: UUID): List<ResourceResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        return resourceRepository.findByIncidentTypeIdAndInstitutionIdAndDeletedFalse(
            incidentTypeId, authDetails.institutionId
        ).map { toResponse(it) }
    }

    private fun getResourceModel(resourceId: UUID): ResourceModel {
        val authDetails = jwtConfigService.getAuthDetails()
        return resourceRepository.findByIdAndInstitutionIdAndDeletedFalse(resourceId, authDetails.institutionId)
            ?: throw NotFoundException("Resource not found")
    }

    private fun uploadFile(file: MultipartFile): FileUploadsModel {
        val maxSize = 10 * 1024 * 1024 // 10MB
        if (file.size > maxSize) {
            throw BadRequestException("File size exceeds 10MB limit")
        }
        val uploadResult = cloudinary.uploader().upload(
            file.bytes,
            ObjectUtils.asMap(
                "folder", "trustline/resources",
                "resource_type", "auto"
            )
        )
        val url = uploadResult["secure_url"] as String
        return fileUploadRepository.save(FileUploadsModel(uploadUrl = url))
    }

    private fun toResponse(resource: ResourceModel) = ResourceResponseDto(
        id = resource.id!!,
        name = resource.name,
        incidentType = resource.incidentType.name,
        contents = resource.contents,
        fileUrl = resource.fileUpload?.uploadUrl,
        createdBy = resource.createdBy.email,
        createdAt = resource.createdAt,
        updatedAt = resource.updatedAt
    )
}
