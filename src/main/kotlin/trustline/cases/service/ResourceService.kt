package trustline.cases.service

import trustline.cases.dto.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface ResourceService {
    fun createResource(request: CreateResourceRequest, file: MultipartFile?): ResourceResponseDto
    fun updateResource(resourceId: UUID, request: UpdateResourceRequest, file: MultipartFile?): ResourceResponseDto
    fun deleteResource(resourceId: UUID)
    fun getResourceById(resourceId: UUID): ResourceResponseDto
    fun getAllResources(): List<ResourceResponseDto>
    fun getResourcesByIncidentType(incidentTypeId: UUID): List<ResourceResponseDto>
}
