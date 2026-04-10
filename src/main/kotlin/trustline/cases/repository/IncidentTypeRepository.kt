package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.IncidentTypesModel
import java.util.*

@Repository
interface IncidentTypeRepository : JpaRepository<IncidentTypesModel, UUID> {
    fun findByNameIgnoreCase(name: String): Optional<IncidentTypesModel>
    fun findByIdAndIsDeletedFalse(id: UUID): Optional<IncidentTypesModel>
    fun findAllByIsDeletedFalse(): List<IncidentTypesModel>
    fun existsByNameIgnoreCase(name: String): Boolean
}
