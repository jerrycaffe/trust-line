package trustline.appuser.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.appuser.dto.Status
import trustline.notification.model.VerificationModel
import java.util.*

@Repository
interface VerificationRepository : JpaRepository<VerificationModel, UUID> {
    fun findByUserIdAndPin(userId: UUID, pin: String): VerificationModel?
    fun findByUserIdAndPinAndStatus(userId: UUID, pin: String, status: Status): VerificationModel?
    fun findByIdAndStatus(id: UUID, status: Status): VerificationModel?
}
