package trustline.cases.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import trustline.cases.dto.*
import trustline.cases.service.ResourceService
import java.util.*

@RestController
@RequestMapping("api/v1/resources")
class ResourceController(
    private val resourceService: ResourceService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('Administrator')")
    fun create(
        @Validated @RequestPart("resource") request: CreateResourceRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResourceResponseDto {
        return resourceService.createResource(request, file)
    }

    @PutMapping("/{resourceId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('Administrator')")
    fun update(
        @PathVariable resourceId: UUID,
        @Validated @RequestPart("resource") request: UpdateResourceRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResourceResponseDto {
        return resourceService.updateResource(resourceId, request, file)
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasAuthority('Administrator')")
    fun delete(@PathVariable resourceId: UUID) {
        resourceService.deleteResource(resourceId)
    }

    @GetMapping("/{resourceId}")
    fun getById(@PathVariable resourceId: UUID): ResourceResponseDto {
        return resourceService.getResourceById(resourceId)
    }

    @GetMapping
    fun getAll(): List<ResourceResponseDto> {
        return resourceService.getAllResources()
    }

    @GetMapping("/incident-type/{incidentTypeId}")
    fun getByIncidentType(@PathVariable incidentTypeId: UUID): List<ResourceResponseDto> {
        return resourceService.getResourcesByIncidentType(incidentTypeId)
    }
}
