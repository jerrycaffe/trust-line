package trustline.cases.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import trustline.appuser.PagedResponse
import trustline.cases.dto.CreateResourceRequest
import trustline.cases.dto.ResourceResponseDto
import trustline.cases.dto.UpdateResourceRequest
import trustline.cases.service.ResourceService
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_RESOURCES
import java.util.*

@RestController
@RequestMapping("api/v1/resources")
class ResourceController(
    private val resourceService: ResourceService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_RESOURCES')")
    fun create(
        @Validated @RequestPart("resource") request: CreateResourceRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResourceResponseDto {
        return resourceService.createResource(request, file)
    }

    @PutMapping("/{resourceId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_RESOURCES')")
    fun update(
        @PathVariable resourceId: UUID,
        @Validated @RequestPart("resource") request: UpdateResourceRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResourceResponseDto {
        return resourceService.updateResource(resourceId, request, file)
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_RESOURCES')")
    fun delete(@PathVariable resourceId: UUID) {
        resourceService.deleteResource(resourceId)
    }

    @GetMapping("/{resourceId}")
    fun getById(@PathVariable resourceId: UUID): ResourceResponseDto {
        return resourceService.getResourceById(resourceId)
    }

    @GetMapping
    fun getAll(
        @RequestParam(value = "offset") offset: Int? = 0,
        @RequestParam(value = "limit") limit: Int? = 20,
    ): PagedResponse<ResourceResponseDto> {
        return resourceService.getAllResources(offset!!, limit!!)
    }

    @GetMapping("/incident-type/{incidentTypeId}")
    fun getByIncidentType(@PathVariable incidentTypeId: UUID): List<ResourceResponseDto> {
        return resourceService.getResourcesByIncidentType(incidentTypeId)
    }
}
