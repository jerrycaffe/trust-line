package trustline.appuser.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trustline.appuser.model.UserModel
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserModel, UUID> {

    @Query("SELECT u FROM UserModel u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    fun findByEmail(@Param("email") email: String): Optional<UserModel>

    fun findByPhoneNumber(phoneNumber: String): Optional<UserModel>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): UserModel?
    fun findByGoogleId(googleId: String): Optional<UserModel>
    fun findByEmailOrPhoneNumberAndInstitutionId(email: String, phoneNumber: String, institutionId: UUID): UserModel?

    @Query("SELECT u FROM UserModel u LEFT JOIN FETCH u.roles WHERE u.email = :email AND u.institution.id = :institutionId")
    fun findByEmailAndInstitutionId(@Param("email") email: String, @Param("institutionId") institutionId: UUID): UserModel?

    @Query("SELECT r.name FROM UserModel u JOIN u.roles r WHERE u.id = :userId")
    fun findRoleNamesByUserId(@Param("userId") userId: UUID): List<String>
}
