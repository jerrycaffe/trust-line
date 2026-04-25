package trustline.institution.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import trustline.config.exception.DuplicateException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.institution.dto.CreateInstitutionDto
import trustline.institution.dto.toInstitutionModel
import trustline.institution.model.InstitutionModel
import trustline.institution.repository.InstitutionRepository
import java.util.*

@Service
class InstitutionServiceImpl(
    private val institutionRepository: InstitutionRepository,
    private val jwtConfigService: JWTConfigService
) : InstitutionService {
    override fun createInstitution(createInstitutionDto: CreateInstitutionDto): InstitutionModel? {
        return try {
            institutionRepository.save(createInstitutionDto.toInstitutionModel())
        } catch (ex: DataIntegrityViolationException) {
            throw DuplicateException("Institution with the name already exists")
        }
    }

    override fun getAllInstitution(): List<InstitutionModel>? {
        return institutionRepository.findAll()
    }

    override fun getInstitutionById(institutionId: UUID): InstitutionModel? {
        return institutionRepository.findByIdOrNull(institutionId)
    }

    override fun getAuthUserInstitution(): InstitutionModel {
        val authDetails = jwtConfigService.getAuthDetails()
        return institutionRepository.findByIdOrNull(authDetails.institutionId)
            ?: throw NotFoundException("Institution not found")
    }
}