package trustline.appuser.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.appuser.model.UserModel
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserModel, UUID> {
    fun findByEmail(email: String): Optional<UserModel>
    fun findByPhoneNumber(phoneNumber: String): Optional<UserModel>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): Optional<UserModel>
    fun findByGoogleId(googleId: String): Optional<UserModel>
}
