package trustline.cases.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.cases.dto.CreateIncidentTypeReq
import trustline.cases.dto.UpdateIncidentTypeReq
import trustline.cases.model.IncidentTypeResponseDto
import trustline.cases.service.IncidentTypeService
import java.util.*

@RestController
@RequestMapping("api/v1/incident-types")
class IncidentTypeController(
    private val incidentTypeService: IncidentTypeService,
) {

    @PostMapping
    @PreAuthorize("hasAuthority('Administrator')")
    fun create(@Validated @RequestBody request: CreateIncidentTypeReq): IncidentTypeResponseDto {
        return incidentTypeService.create(request)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('Administrator')")
    fun update(@PathVariable id: UUID, @Validated @RequestBody request: UpdateIncidentTypeReq): IncidentTypeResponseDto {
        return incidentTypeService.update(id, request)
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
    @PreAuthorize("hasAuthority('Administrator')")
    fun delete(@PathVariable id: UUID) {
        incidentTypeService.delete(id)
    }
}
