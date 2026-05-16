package trustline.cases.service

import trustline.appuser.PagedResponse
import trustline.cases.dto.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface ResourceService {
    fun createResource(request: CreateResourceRequest, file: MultipartFile?): ResourceResponseDto
    fun updateResource(resourceId: UUID, request: UpdateResourceRequest, file: MultipartFile?): ResourceResponseDto
    fun deleteResource(resourceId: UUID)
    fun getResourceById(resourceId: UUID): ResourceResponseDto
    fun getAllResources(offset: Int, limit: Int): PagedResponse<ResourceResponseDto>
    fun getResourcesByIncidentType(incidentTypeId: UUID): List<ResourceResponseDto>
}
