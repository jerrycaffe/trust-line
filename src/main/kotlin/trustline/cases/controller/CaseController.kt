package trustline.cases.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import trustline.cases.dto.CaseResponseDto
import trustline.cases.dto.CommentResponseDto
import trustline.cases.dto.CreateCaseDto
import trustline.cases.dto.CreateCommentRequest
import trustline.cases.service.CaseService
import java.util.*

@RestController
@RequestMapping("api/v1/cases")
class CaseController(
    private val caseService: CaseService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun create(
        @Validated @RequestPart("case") request: CreateCaseDto,
        @RequestPart("files", required = false) files: List<MultipartFile>?
    ): CaseResponseDto {
        return caseService.createCase(request, files)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): CaseResponseDto {
        return caseService.getCaseById(id)
    }

    @GetMapping("/my-cases")
    fun getMyCases(): List<CaseResponseDto> {
        return caseService.getMyCases()
    }

    @GetMapping
    @PreAuthorize("hasAuthority('Administrator')")
    fun getAllCases(): List<CaseResponseDto> {
        return caseService.getAllCases()
    }

    @PostMapping("/{caseId}/comments")
    @PreAuthorize("hasAuthority('Administrator')")
    fun addComment(
        @PathVariable caseId: UUID,
        @Validated @RequestBody request: CreateCommentRequest
    ): CommentResponseDto {
        return caseService.addComment(caseId, request)
    }

    @PutMapping("/{caseId}/close")
    @PreAuthorize("hasAuthority('Administrator')")
    fun closeCase(@PathVariable caseId: UUID): CaseResponseDto {
        return caseService.closeCase(caseId)
    }

    @PutMapping("/{caseId}/reopen")
    @PreAuthorize("hasAuthority('Administrator')")
    fun reopenCase(@PathVariable caseId: UUID): CaseResponseDto {
        return caseService.reopenCase(caseId)
    }
}
