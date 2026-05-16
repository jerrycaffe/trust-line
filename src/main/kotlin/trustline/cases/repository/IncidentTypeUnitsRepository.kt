package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.IncidentTypeUnitId
import trustline.cases.model.IncidentTypeUnitsModel
import java.util.*

@Repository
interface IncidentTypeUnitsRepository : JpaRepository<IncidentTypeUnitsModel, IncidentTypeUnitId> {
    fun findByIncidentTypeId(incidentTypeId: UUID): List<IncidentTypeUnitsModel>
}
