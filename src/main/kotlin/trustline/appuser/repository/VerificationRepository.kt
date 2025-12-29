package trustline.appuser.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.appuser.model.VerificationModel
import java.util.*

@Repository
interface VerificationRepository : JpaRepository<VerificationModel, UUID> {
    fun findByUserIdAndPin(userId: UUID, pin: String): Optional<VerificationModel>
}
