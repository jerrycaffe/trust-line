package trustline.cases.model

import jakarta.persistence.*


@Entity
@Table(name = "incident_type_units")
data class IncidentTypeUnitsModel(
    @EmbeddedId
    val id: IncidentTypesModel? = null,

    @ManyToOne
    @MapsId("incidentTypeId")
    @JoinColumn(name = "incident_type_id")
    val incidentType: IncidentTypesModel? = null,

    @ManyToOne
    @MapsId("unitId")
    @JoinColumn(name = "unit_id")
    val unit: Unit? = null,

    @Column(name = "purpose")
    val purpose: String? = null
)
