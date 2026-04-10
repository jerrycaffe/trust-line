package trustline.cases.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import trustline.appuser.repository.UserRepository
import trustline.cases.service.CaseService

@RestController
@RequestMapping("api/v1/cases")
class CaseController(
    private val caseService: CaseService,
    private val userRepository: UserRepository
) {

//    @PostMapping
//    fun create(
//        @Validated @RequestBody dto: CreateCaseDto,
//        @AuthenticationPrincipal userDetails: UserDetails
//    ): ResponseEntity<CaseResponseDto> {
//        val user = userRepository.findByEmail(userDetails.username)
//            .orElseThrow { NotFoundException("User not found") }
//        val response = caseService.create(dto, user.id!!)
//        return ResponseEntity.status(HttpStatus.CREATED).body(response)
//    }

//    @PutMapping("/{id}")
//    fun update(
//        @PathVariable id: UUID,
//        @Validated @RequestBody dto: UpdateCaseDto
//    ): ResponseEntity<CaseResponseDto> {
//        val response = caseService.update(id, dto)
//        return ResponseEntity.ok(response)
//    }
//
//    @GetMapping("/{id}")
//    fun getById(@PathVariable id: UUID): ResponseEntity<CaseResponseDto> {
//        val response = caseService.getById(id)
//        return ResponseEntity.ok(response)
//    }

//    @GetMapping
//    fun getAll(): ResponseEntity<List<CaseResponseDto>> {
//        val response = caseService.getAll()
//        return ResponseEntity.ok(response)
//    }

//    @GetMapping("/my-cases")
//    fun getMyCases(
//        @AuthenticationPrincipal userDetails: UserDetails
//    ): ResponseEntity<List<CaseResponseDto>> {
//        val user = userRepository.findByEmail(userDetails.username)
//            .orElseThrow { NotFoundException("User not found") }
//        val response = caseService.getByReportedBy(user.id!!)
//        return ResponseEntity.ok(response)
//    }

//    @GetMapping("/by-incident-type/{incidentTypeId}")
//    fun getByIncidentType(
//        @PathVariable incidentTypeId: UUID
//    ): ResponseEntity<List<CaseResponseDto>> {
//        val response = caseService.getByIncidentType(incidentTypeId)
//        return ResponseEntity.ok(response)
//    }

//    @DeleteMapping("/{id}")
//    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
//        caseService.delete(id)
//        return ResponseEntity.noContent().build()
//    }
}
