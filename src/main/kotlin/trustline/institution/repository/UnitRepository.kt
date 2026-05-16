package trustline.institution.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.institution.model.UnitModel
import java.util.*

@Repository
interface UnitRepository : JpaRepository<UnitModel, UUID> {
    fun findByInstitutionId(institutionId: UUID): List<UnitModel>
    fun findByIdAndInstitutionId(id: UUID, institutionId: UUID): UnitModel?
    fun existsByNameAndInstitutionId(name: String, institutionId: UUID): Boolean
}
