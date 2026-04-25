package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.CaseFileUploadsModel
import java.util.*

@Repository
interface CaseFileUploadRepository : JpaRepository<CaseFileUploadsModel, UUID> {
    fun findByCaseId(caseId: UUID): List<CaseFileUploadsModel>
}
