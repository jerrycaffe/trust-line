package trustline.cases.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import trustline.appuser.service.UserService
import trustline.cases.dto.CreateIncidentTypeReq
import trustline.cases.dto.UpdateIncidentTypeReq
import trustline.cases.dto.toIncidentTypeModel
import trustline.cases.model.IncidentTypeResponseDto
import trustline.cases.model.toIncidentTypeResponse
import trustline.cases.repository.IncidentTypeRepository
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.service.InstitutionService
import java.util.*

@Service
class IncidentTypeServiceImpl(
    private val incidentTypeRepository: IncidentTypeRepository,
    private val jwtConfigService: JWTConfigService,
    private val institutionService: InstitutionService,
    private val userService: UserService
) : IncidentTypeService {

    @Transactional
    override fun create(request: CreateIncidentTypeReq): IncidentTypeResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val institution = institutionService.getAuthUserInstitution()
        val createdBy = userService.getUserById(authDetails.userId)

        if (incidentTypeRepository.existsByNameAndInstitutionId(request.name!!, institution.id!!)) {
            throw BadRequestException("Incident type with name '${request.name}' already exists")
        }

        val incidentType = request.toIncidentTypeModel(institution, createdBy)
        return incidentTypeRepository.save(incidentType).toIncidentTypeResponse()
    }

    @Transactional
    override fun update(id: UUID, request: UpdateIncidentTypeReq): IncidentTypeResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val incidentType = incidentTypeRepository.findByIdAndInstitutionIdAndDeletedFalse(id, authDetails.institutionId)
            ?: throw NotFoundException("Incident type not found")

        request.name?.let { newName ->
            if (newName != incidentType.name &&
                incidentTypeRepository.existsByNameAndInstitutionId(newName, authDetails.institutionId)
            ) {
                throw BadRequestException("Incident type with name '$newName' already exists")
            }
            incidentType.name = newName
        }
        request.description?.let { incidentType.description = it }
        request.steps?.let { incidentType.steps = it }

        return incidentTypeRepository.save(incidentType).toIncidentTypeResponse()
    }

    override fun getById(id: UUID): IncidentTypeResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val incidentType = incidentTypeRepository.findByIdAndInstitutionIdAndDeletedFalse(id, authDetails.institutionId)
            ?: throw NotFoundException("Incident type not found")
        return incidentType.toIncidentTypeResponse()
    }

    override fun getAll(): List<IncidentTypeResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        return incidentTypeRepository.findByInstitutionIdAndDeletedFalse(authDetails.institutionId)
            .map { it.toIncidentTypeResponse() }
    }

    @Transactional
    override fun delete(id: UUID) {
        val authDetails = jwtConfigService.getAuthDetails()
        val incidentType = incidentTypeRepository.findByIdAndInstitutionIdAndDeletedFalse(id, authDetails.institutionId)
            ?: throw NotFoundException("Incident type not found")

        incidentType.deleted = true
        incidentTypeRepository.save(incidentType)
    }
}
