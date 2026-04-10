package trustline.cases.controller

//import trustline.cases.dto.IncidentTypeResponseDto
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import trustline.appuser.repository.UserRepository
import trustline.cases.service.IncidentTypeService

@RestController
@RequestMapping("api/v1/incident-types")
class IncidentTypeController(
    private val incidentTypeService: IncidentTypeService,
    private val userRepository: UserRepository
) {
//
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    fun create(
//        @Validated @RequestBody dto: CreateIncidentTypeDto,
//        @AuthenticationPrincipal userDetails: UserDetails
//    ): ResponseEntity<IncidentTypeResponseDto> {
//        val user = userRepository.findByEmail(userDetails.username)
//            .orElseThrow { NotFoundException("User not found") }
//        val response = incidentTypeService.create(dto, user.id!!)
//        return ResponseEntity.status(HttpStatus.CREATED).body(response)
//    }

//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    fun update(
//        @PathVariable id: UUID,
//        @Validated @RequestBody dto: UpdateIncidentTypeDto
//    ): ResponseEntity<IncidentTypeResponseDto> {
//        val response = incidentTypeService.update(id, dto)
//        return ResponseEntity.ok(response)
//    }

//    @GetMapping("/{id}")
//    fun getById(@PathVariable id: UUID): ResponseEntity<IncidentTypeResponseDto> {
//        val response = incidentTypeService.getById(id)
//        return ResponseEntity.ok(response)
//    }

//    @GetMapping
//    fun getAll(): ResponseEntity<List<IncidentTypeResponseDto>> {
//        val response = incidentTypeService.getAll()
//        return ResponseEntity.ok(response)
//    }

//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
//        incidentTypeService.delete(id)
//        return ResponseEntity.noContent().build()
//    }
}
