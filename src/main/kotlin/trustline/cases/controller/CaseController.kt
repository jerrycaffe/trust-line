package trustline.cases.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import trustline.appuser.PagedResponse
import trustline.appuser.dto.Status
import trustline.cases.dto.*
import trustline.cases.service.CaseService
import trustline.config.security.AuthDetailsResponse
import trustline.config.security.CurrentUser
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_CASES
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("api/v1/cases")
class CaseController(
    private val caseService: CaseService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun create(
        @CurrentUser authDetails: AuthDetailsResponse,
        @Validated @RequestPart("case") request: CreateCaseDto,
        @RequestPart("files", required = false) files: List<MultipartFile>?
    ): CaseResponseDto {
        return caseService.createCase(authDetails, request, files)
    }

    @GetMapping("/{id}")
    fun getById(@CurrentUser authDetails: AuthDetailsResponse, @PathVariable id: UUID): CaseResponseDto {
        return caseService.getCaseById(authDetails, id)
    }

    @GetMapping("/my-cases")
    fun getMyCases(
        @CurrentUser authDetails: AuthDetailsResponse,
        @RequestParam(value = "offset") offset: Int? = 0,
        @RequestParam(value = "limit") limit: Int? = 20,
    ): PagedResponse<CaseResponseDto> {
        return caseService.getMyCases(authDetails, offset!!, limit!!)
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_CASES')")
    fun getAllCases(
        @CurrentUser authDetails: AuthDetailsResponse,
        @RequestParam(value = "offset") offset: Int? = 0,
        @RequestParam(value = "limit") limit: Int? = 20,
        @RequestParam(value = "status", required = false) status: Status? = null,
        @RequestParam(value = "incidentTypeId", required = false) incidentTypeId: UUID? = null,
        @RequestParam(value = "startDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate? = null,
        @RequestParam(value = "startdate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDateLower: LocalDate? = null,
        @RequestParam(value = "strtdate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDateTypo: LocalDate? = null,
        @RequestParam(value = "endDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate? = null,
        @RequestParam(value = "enddate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDateLower: LocalDate? = null,
    ): PagedResponse<CaseResponseDto> {
        val effectiveStartDate = startDate ?: startDateLower ?: startDateTypo
        val effectiveEndDate = endDate ?: endDateLower

        return caseService.getAllCases(
            authDetails,
            offset!!,
            limit!!,
            status,
            incidentTypeId,
            effectiveStartDate,
            effectiveEndDate
        )
    }

    @PostMapping("/{caseId}/comments")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_CASES')")
    fun addComment(
        @CurrentUser authDetails: AuthDetailsResponse,
        @PathVariable caseId: UUID,
        @Validated @RequestBody request: CreateCommentRequest
    ): CommentResponseDto {
        return caseService.addComment(authDetails, caseId, request)
    }

    @PutMapping("/{caseId}/conclude")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_CASES')")
    fun concludeCase(
        @CurrentUser authDetails: AuthDetailsResponse,
        @PathVariable caseId: UUID,
        @Validated @RequestBody request: ConcludeCaseRequest
    ): CaseResponseDto {
        return caseService.concludeCase(authDetails, caseId, request)
    }

    @PutMapping("/{caseId}/close")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_CASES')")
    fun closeCase(@CurrentUser authDetails: AuthDetailsResponse, @PathVariable caseId: UUID): CaseResponseDto {
        return caseService.closeCase(authDetails, caseId)
    }

    @PutMapping("/{caseId}/reopen")
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_CASES')")
    fun reopenCase(@CurrentUser authDetails: AuthDetailsResponse, @PathVariable caseId: UUID): CaseResponseDto {
        return caseService.reopenCase(authDetails, caseId)
    }
}
