package trustline.cases.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
//import trustline.cases.dto.CaseResponseDto
import trustline.cases.dto.CreateCaseDto
import trustline.cases.dto.UpdateCaseDto
import trustline.cases.repository.CaseRepository
import trustline.cases.repository.IncidentTypeRepository
import trustline.config.exception.NotFoundException
import java.util.*

@Service
class CaseServiceImpl(
    private val caseRepository: CaseRepository,
    private val incidentTypeRepository: IncidentTypeRepository
) : CaseService {

//    @Transactional
//    override fun create(dto: CreateCaseDto, reportedBy: UUID): CaseResponseDto {
//        val incidentType = incidentTypeRepository.findByIdAndIsDeletedFalse(dto.incidentTypeId)
//            .orElseThrow { NotFoundException("Incident type not found with id: ${dto.incidentTypeId}") }
//
//        val case = Case(
//            incidentType = incidentType,
//            dateOfIncident = dto.dateOfIncident,
//            location = dto.location,
//            description = dto.description,
//            uploadUrl = dto.uploadUrl,
//            reportedBy = reportedBy
//        )
//
//        val savedCase = caseRepository.save(case)
//        return CaseResponseDto.fromEntity(savedCase)
//    }
//
//    @Transactional
//    override fun update(id: UUID, dto: UpdateCaseDto): CaseResponseDto {
//        val case = caseRepository.findByIdAndIsDeletedFalse(id)
//            .orElseThrow { NotFoundException("Case not found with id: $id") }
//
//        dto.incidentTypeId?.let { incidentTypeId ->
//            val incidentType = incidentTypeRepository.findByIdAndIsDeletedFalse(incidentTypeId)
//                .orElseThrow { NotFoundException("Incident type not found with id: $incidentTypeId") }
//            case.incidentType = incidentType
//        }
//
//        dto.dateOfIncident?.let { case.dateOfIncident = it }
//        dto.location?.let { case.location = it }
//        dto.description?.let { case.description = it }
//        dto.uploadUrl?.let { case.uploadUrl = it }
//
//        val updatedCase = caseRepository.save(case)
//        return CaseResponseDto.fromEntity(updatedCase)
//    }

//    override fun getById(id: UUID): CaseResponseDto {
//        val case = caseRepository.findByIdAndIsDeletedFalse(id)
//            .orElseThrow { NotFoundException("Case not found with id: $id") }
//        return CaseResponseDto.fromEntity(case)
//    }
//
//    override fun getAll(): List<CaseResponseDto> {
//        return caseRepository.findAllByIsDeletedFalse()
//            .map { CaseResponseDto.fromEntity(it) }
//    }
//
//    override fun getByReportedBy(reportedBy: UUID): List<CaseResponseDto> {
//        return caseRepository.findAllByReportedByAndIsDeletedFalse(reportedBy)
//            .map { CaseResponseDto.fromEntity(it) }
//    }
//
//    override fun getByIncidentType(incidentTypeId: UUID): List<CaseResponseDto> {
//        return caseRepository.findAllByIncidentTypeIdAndIsDeletedFalse(incidentTypeId)
//            .map { CaseResponseDto.fromEntity(it) }
//    }

//    @Transactional
//    override fun delete(id: UUID) {
//        val case = caseRepository.findByIdAndIsDeletedFalse(id)
//            .orElseThrow { NotFoundException("Case not found with id: $id") }
//
//        case.isDeleted = true
//        caseRepository.save(case)
//    }
}
