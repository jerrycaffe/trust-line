package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.CasesModel
import java.util.*

@Repository
interface CaseRepository : JpaRepository<CasesModel, UUID> {
    fun findByUserIdAndInstitutionIdAndIsDeletedFalse(userId: UUID, institutionId: UUID): List<CasesModel>
    fun findByInstitutionIdAndIsDeletedFalse(institutionId: UUID): List<CasesModel>
    fun findByIdAndInstitutionIdAndIsDeletedFalse(id: UUID, institutionId: UUID): CasesModel?
}
