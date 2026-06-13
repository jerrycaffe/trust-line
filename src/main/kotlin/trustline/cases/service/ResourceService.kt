package trustline.cases.service

import org.springframework.web.multipart.MultipartFile
import trustline.appuser.PagedResponse
import trustline.cases.dto.CreateResourceRequest
import trustline.cases.dto.ResourceResponseDto
import trustline.cases.dto.UpdateResourceRequest
import java.util.*

interface ResourceService {
    fun createResource(request: CreateResourceRequest, file: MultipartFile?): ResourceResponseDto
    fun updateResource(resourceId: UUID, request: UpdateResourceRequest, file: MultipartFile?): ResourceResponseDto
    fun deleteResource(resourceId: UUID)
    fun getResourceById(resourceId: UUID): ResourceResponseDto
    fun getAllResources(offset: Int, limit: Int): PagedResponse<ResourceResponseDto>
    fun getResourcesByIncidentType(incidentTypeId: UUID): List<ResourceResponseDto>
}
