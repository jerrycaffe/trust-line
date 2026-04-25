package trustline.institution.service

import trustline.institution.dto.CreateInstitutionDto
import trustline.institution.model.InstitutionModel
import java.util.*

interface InstitutionService {
    fun createInstitution(createInstitutionDto: CreateInstitutionDto): InstitutionModel?
    fun getAllInstitution(): List<InstitutionModel>?
    fun getInstitutionById(institutionId: UUID): InstitutionModel?
    fun getAuthUserInstitution(): InstitutionModel
}