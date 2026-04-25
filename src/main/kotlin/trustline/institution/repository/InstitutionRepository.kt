package trustline.institution.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.institution.model.InstitutionModel
import java.util.*

@Repository
interface InstitutionRepository : JpaRepository<InstitutionModel, UUID> {
    fun findByName(name: String): InstitutionModel?
}