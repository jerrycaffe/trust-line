package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.CasesModel
import java.util.*

@Repository
interface CaseRepository : JpaRepository<CasesModel, UUID> {
    fun findByIdAndIsDeletedFalse(id: UUID): Optional<CasesModel>
    fun findAllByIsDeletedFalse(): List<CasesModel>
    fun findAllByReportedByAndIsDeletedFalse(reportedBy: UUID): List<CasesModel>
    fun findAllByIncidentTypeIdAndIsDeletedFalse(incidentTypeId: UUID): List<CasesModel>
}
