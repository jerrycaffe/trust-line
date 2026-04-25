package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.IncidentTypesModel
import java.util.*

@Repository
interface IncidentTypeRepository : JpaRepository<IncidentTypesModel, UUID> {
    fun findByNameAndInstitutionId(name: String, institutionId: UUID): IncidentTypesModel?
    fun existsByNameAndInstitutionId(name: String, institutionId: UUID): Boolean
    fun findByIdAndDeletedFalse(id: UUID): IncidentTypesModel?
    fun findByIdAndInstitutionIdAndDeletedFalse(id: UUID, institutionId: UUID): IncidentTypesModel?
    fun findByInstitutionIdAndDeletedFalse(institutionId: UUID): List<IncidentTypesModel>
}
