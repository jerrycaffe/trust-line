package trustline.cases.service

//import trustline.cases.dto.IncidentTypeResponseDto
import org.springframework.stereotype.Service
import trustline.cases.repository.IncidentTypeRepository

@Service
class IncidentTypeServiceImpl(
    private val incidentTypeRepository: IncidentTypeRepository
) : IncidentTypeService {

//    @Transactional
//    override fun create(dto: CreateIncidentTypeDto, createdBy: UUID): IncidentTypeResponseDto {
//        if (incidentTypeRepository.existsByNameIgnoreCase(dto.name)) {
//            throw BadRequestException("Incident type with name '${dto.name}' already exists")
//        }
//
//        val incidentType = IncidentType(
//            name = dto.name,
//            description = dto.description,
//            createdBy = createdBy
//        )
//
//        val savedIncidentType = incidentTypeRepository.save(incidentType)
//        return IncidentTypeResponseDto.fromEntity(savedIncidentType)
//    }

//    @Transactional
//    override fun update(id: UUID, dto: UpdateIncidentTypeDto): IncidentTypeResponseDto {
//        val incidentType = incidentTypeRepository.findByIdAndIsDeletedFalse(id)
//            .orElseThrow { NotFoundException("Incident type not found with id: $id") }
//
//        dto.name?.let { newName ->
//            if (newName != incidentType.name && incidentTypeRepository.existsByNameIgnoreCase(newName)) {
//                throw BadRequestException("Incident type with name '$newName' already exists")
//            }
//            incidentType.name = newName
//        }
//
//        dto.description?.let { incidentType.description = it }
//
//        val updatedIncidentType = incidentTypeRepository.save(incidentType)
//        return IncidentTypeResponseDto.fromEntity(updatedIncidentType)
//    }

//    override fun getById(id: UUID): IncidentTypeResponseDto {
//        val incidentType = incidentTypeRepository.findByIdAndIsDeletedFalse(id)
//            .orElseThrow { NotFoundException("Incident type not found with id: $id") }
//        return IncidentTypeResponseDto.fromEntity(incidentType)
//    }
//
//    override fun getAll(): List<IncidentTypeResponseDto> {
//        return incidentTypeRepository.findAllByIsDeletedFalse()
//            .map { IncidentTypeResponseDto.fromEntity(it) }
//    }
//
//    @Transactional
//    override fun delete(id: UUID) {
//        val incidentType = incidentTypeRepository.findByIdAndIsDeletedFalse(id)
//            .orElseThrow { NotFoundException("Incident type not found with id: $id") }
//
//        incidentType.isDeleted = true
//        incidentTypeRepository.save(incidentType)
//    }
}
