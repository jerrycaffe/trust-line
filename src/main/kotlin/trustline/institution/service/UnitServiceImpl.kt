package trustline.institution.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.dto.CreateUnitReq
import trustline.institution.dto.UpdateUnitReq
import trustline.institution.dto.UnitResponseDto
import trustline.institution.dto.toUnitResponse
import trustline.institution.model.UnitModel
import trustline.institution.repository.UnitRepository
import java.util.UUID

@Service
class UnitServiceImpl(
    private val unitRepository: UnitRepository,
    private val institutionService: InstitutionService,
    private val jwtConfigService: JWTConfigService
) : UnitService {

    @Transactional
    override fun create(request: CreateUnitReq): UnitResponseDto {
        val institution = institutionService.getAuthUserInstitution()

        if (unitRepository.existsByNameAndInstitutionId(request.name!!, institution.id!!)) {
            throw BadRequestException("Unit with name '${request.name}' already exists")
        }

        val unit = UnitModel(name = request.name, institution = institution)
        return unitRepository.save(unit).toUnitResponse()
    }

    @Transactional
    override fun update(id: UUID, request: UpdateUnitReq): UnitResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val unit = unitRepository.findByIdAndInstitutionId(id, authDetails.institutionId)
            ?: throw NotFoundException("Unit not found")

        if (request.name != null && request.name != unit.name &&
            unitRepository.existsByNameAndInstitutionId(request.name, authDetails.institutionId)
        ) {
            throw BadRequestException("Unit with name '${request.name}' already exists")
        }

        unit.name = request.name
        return unitRepository.save(unit).toUnitResponse()
    }

    @Transactional
    override fun delete(id: UUID) {
        val authDetails = jwtConfigService.getAuthDetails()
        val unit = unitRepository.findByIdAndInstitutionId(id, authDetails.institutionId)
            ?: throw NotFoundException("Unit not found")
        unitRepository.delete(unit)
    }

    override fun getAll(): List<UnitResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        return unitRepository.findByInstitutionId(authDetails.institutionId)
            .map { it.toUnitResponse() }
    }
}
