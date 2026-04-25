package trustline.institution.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.institution.dto.CreateInstitutionDto
import trustline.institution.model.InstitutionModel
import trustline.institution.service.InstitutionService

@RestController
@RequestMapping("/api/v1/institution")
class InstitutionController(
    private val institutionService: InstitutionService
) {
    @PostMapping
    fun createInstitution(@RequestBody @Validated createInstitutionReq: CreateInstitutionDto): InstitutionModel? {
        return institutionService.createInstitution(createInstitutionReq)
    }

    @GetMapping
    fun getAllInstitution(): List<InstitutionModel>? {
        return institutionService.getAllInstitution()
    }
}