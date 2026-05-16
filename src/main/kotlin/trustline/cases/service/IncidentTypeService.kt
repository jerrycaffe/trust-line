package trustline.cases.service

import trustline.cases.dto.CreateIncidentTypeReq
import trustline.cases.dto.UpdateIncidentTypeReq
import trustline.cases.dto.UpdateIncidentTypeUnitsReq
import trustline.cases.model.IncidentTypeResponseDto
import java.util.*

interface IncidentTypeService {
    fun create(request: CreateIncidentTypeReq): IncidentTypeResponseDto
    fun update(id: UUID, request: UpdateIncidentTypeReq): IncidentTypeResponseDto
    fun updateUnits(id: UUID, request: UpdateIncidentTypeUnitsReq): IncidentTypeResponseDto
    fun getById(id: UUID): IncidentTypeResponseDto
    fun getAll(): List<IncidentTypeResponseDto>
    fun delete(id: UUID)
}
