package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.CommentsModel
import java.util.*

@Repository
interface CommentRepository : JpaRepository<CommentsModel, UUID> {
    fun findByCaseIdOrderByCreatedAtAsc(caseId: UUID): List<CommentsModel>
}
