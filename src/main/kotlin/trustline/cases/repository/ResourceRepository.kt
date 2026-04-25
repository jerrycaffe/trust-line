package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import trustline.cases.model.ResourceModel
import java.util.*

@Repository
interface ResourceRepository : JpaRepository<ResourceModel, UUID> {
    fun findByIdAndInstitutionIdAndDeletedFalse(id: UUID, institutionId: UUID): ResourceModel?
    fun findByInstitutionIdAndDeletedFalse(institutionId: UUID): List<ResourceModel>
    fun findByIncidentTypeIdAndInstitutionIdAndDeletedFalse(incidentTypeId: UUID, institutionId: UUID): List<ResourceModel>
}
