package trustline.cases.model

import jakarta.persistence.*
import trustline.institution.model.UnitModel
import java.util.*


@Entity
@Table(name = "incident_type_units")
data class IncidentTypeUnitsModel(
    @EmbeddedId
    val id: IncidentTypeUnitId? = null,

    @ManyToOne
    @MapsId("incidentTypeId")
    @JoinColumn(name = "incident_type_id")
    val incidentType: IncidentTypesModel? = null,

    @ManyToOne
    @MapsId("unitId")
    @JoinColumn(name = "unit_id")
    val unit: UnitModel? = null,

    @Column(name = "purpose")
    val purpose: String? = null
)

@Embeddable
data class IncidentTypeUnitId(

    @Column(name = "incident_type_id")
    val incidentTypeId: UUID? = null,

    @Column(name = "unit_id")
    val unitId: UUID? = null
)
