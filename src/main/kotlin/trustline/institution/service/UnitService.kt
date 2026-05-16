package trustline.institution.service

import trustline.institution.dto.CreateUnitReq
import trustline.institution.dto.UpdateUnitReq
import trustline.institution.dto.UnitResponseDto
import java.util.UUID

interface UnitService {
    fun create(request: CreateUnitReq): UnitResponseDto
    fun update(id: UUID, request: UpdateUnitReq): UnitResponseDto
    fun delete(id: UUID)
    fun getAll(): List<UnitResponseDto>
}
