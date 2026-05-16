package trustline.cases.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.cases.dto.CreateIncidentTypeReq
import trustline.cases.dto.UpdateIncidentTypeReq
import trustline.cases.dto.UpdateIncidentTypeUnitsReq
import trustline.cases.model.IncidentTypeResponseDto
import trustline.cases.service.IncidentTypeService
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_INCIDENT_TYPES
import java.util.*

@RestController
@RequestMapping("api/v1/incident-types")
class IncidentTypeController(
    private val incidentTypeService: IncidentTypeService,
) {

    @PostMapping
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_INCIDENT_TYPES')")
    fun create(@Validated @RequestBody request: CreateIncidentTypeReq): IncidentTypeResponseDto {
        return incidentTypeService.create(request)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_INCIDENT_TYPES')")
    fun update(@PathVariable id: UUID, @Validated @RequestBody request: UpdateIncidentTypeReq): IncidentTypeResponseDto {
        return incidentTypeService.update(id, request)
    }

    @PutMapping("/{id}/units")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_INCIDENT_TYPES')")
    fun updateUnits(
        @PathVariable id: UUID,
        @Validated @RequestBody request: UpdateIncidentTypeUnitsReq
    ): IncidentTypeResponseDto {
        return incidentTypeService.updateUnits(id, request)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): IncidentTypeResponseDto {
        return incidentTypeService.getById(id)
    }

    @GetMapping
    fun getAll(): List<IncidentTypeResponseDto> {
        return incidentTypeService.getAll()
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_INCIDENT_TYPES')")
    fun delete(@PathVariable id: UUID) {
        incidentTypeService.delete(id)
    }
}
