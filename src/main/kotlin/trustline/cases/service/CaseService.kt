package trustline.cases.service

import org.springframework.web.multipart.MultipartFile
import trustline.cases.dto.CaseResponseDto
import trustline.cases.dto.CommentResponseDto
import trustline.cases.dto.CreateCaseDto
import trustline.cases.dto.CreateCommentRequest
import java.util.*

interface CaseService {
    fun createCase(request: CreateCaseDto, files: List<MultipartFile>?): CaseResponseDto
    fun getCaseById(id: UUID): CaseResponseDto
    fun getMyCases(): List<CaseResponseDto>
    fun getAllCases(): List<CaseResponseDto>
    fun addComment(caseId: UUID, request: CreateCommentRequest): CommentResponseDto
    fun closeCase(caseId: UUID): CaseResponseDto
    fun reopenCase(caseId: UUID): CaseResponseDto
}
