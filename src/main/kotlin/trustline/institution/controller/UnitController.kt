package trustline.institution.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_UNITS
import trustline.institution.dto.CreateUnitReq
import trustline.institution.dto.UnitResponseDto
import trustline.institution.dto.UpdateUnitReq
import trustline.institution.service.UnitService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/units")
class UnitController(
    private val unitService: UnitService
) {

    @PostMapping
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_UNITS')")
    fun create(@Validated @RequestBody request: CreateUnitReq): UnitResponseDto {
        return unitService.create(request)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_UNITS')")
    fun update(@PathVariable id: UUID, @Validated @RequestBody request: UpdateUnitReq): UnitResponseDto {
        return unitService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_UNITS')")
    fun delete(@PathVariable id: UUID) {
        unitService.delete(id)
    }

    @GetMapping
    fun getAll(): List<UnitResponseDto> {
        return unitService.getAll()
    }
}
