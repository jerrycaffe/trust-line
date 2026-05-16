package trustline.cases.service

import org.springframework.web.multipart.MultipartFile
import trustline.appuser.PagedResponse
import trustline.appuser.dto.Status
import trustline.cases.dto.*
import trustline.config.security.AuthDetailsResponse
import java.time.LocalDate
import java.util.*

interface CaseService {
    fun createCase(authDetails: AuthDetailsResponse, request: CreateCaseDto, files: List<MultipartFile>?): CaseResponseDto
    fun getCaseById(authDetails: AuthDetailsResponse, id: UUID): CaseResponseDto
    fun getMyCases(authDetails: AuthDetailsResponse, offset: Int, limit: Int): PagedResponse<CaseResponseDto>
    fun getAllCases(
        authDetails: AuthDetailsResponse,
        offset: Int,
        limit: Int,
        status: Status?,
        incidentTypeId: UUID?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): PagedResponse<CaseResponseDto>
    fun addComment(authDetails: AuthDetailsResponse, caseId: UUID, request: CreateCommentRequest): CommentResponseDto
    fun concludeCase(authDetails: AuthDetailsResponse, caseId: UUID, request: ConcludeCaseRequest): CaseResponseDto
    fun closeCase(authDetails: AuthDetailsResponse, caseId: UUID): CaseResponseDto
    fun reopenCase(authDetails: AuthDetailsResponse, caseId: UUID): CaseResponseDto
}
