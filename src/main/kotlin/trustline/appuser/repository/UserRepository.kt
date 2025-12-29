package trustline.appuser.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.appuser.model.User
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun findByPhoneNumber(phoneNumber: String): Optional<User>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): Optional<User>
    fun findByGoogleId(googleId: String): Optional<User>
}
