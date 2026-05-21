package trustline.appuser.repository;

import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trustline.appuser.PageRequest
import trustline.appuser.dto.Gender
import trustline.appuser.model.UserModel
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserModel, UUID> {

    @Query("SELECT u FROM UserModel u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    fun findByEmail(@Param("email") email: String): Optional<UserModel>

    fun findAllByEmail(email: String): List<UserModel>

    fun findByPhoneNumber(phoneNumber: String): Optional<UserModel>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): UserModel?
    fun findByGoogleId(googleId: String): Optional<UserModel>
    fun findByEmailOrPhoneNumberAndInstitutionId(email: String, phoneNumber: String, institutionId: UUID): UserModel?

    @Query("SELECT u FROM UserModel u LEFT JOIN FETCH u.roles WHERE u.email = :email AND u.institution.id = :institutionId")
    fun findByEmailAndInstitutionId(@Param("email") email: String, @Param("institutionId") institutionId: UUID): UserModel?

    @Query("SELECT u FROM UserModel u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.institution.id = :institutionId")
    fun findByIdAndInstitutionId(@Param("id") id: UUID, @Param("institutionId") institutionId: UUID): UserModel?

    @Query(
        "SELECT DISTINCT u FROM UserModel u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.email = :email AND u.institution.id = :institutionId"
    )
    fun findWithAuthoritiesByEmailAndInstitutionId(
        @Param("email") email: String,
        @Param("institutionId") institutionId: UUID
    ): UserModel?

    @Query("SELECT r.name FROM UserModel u JOIN u.roles r WHERE u.id = :userId")
    fun findRoleNamesByUserId(@Param("userId") userId: UUID): List<String>

    @Query("SELECT DISTINCT p.name FROM UserModel u JOIN u.roles r JOIN r.permissions p WHERE u.id = :userId")
    fun findPermissionNamesByUserId(@Param("userId") userId: UUID): List<String>

        @Query(
                """
                SELECT u
                FROM UserModel u
                WHERE u.institution.id = :institutionId
                    AND u.isDeleted = false
                    AND EXISTS (
                        SELECT 1
                        FROM u.roles r
                        WHERE r.name = 'User'
                    )
                    AND NOT EXISTS (
                        SELECT 1
                        FROM u.roles r2
                        WHERE r2.name <> 'User'
                    )
                """
        )
        fun findNonAdminUsersByInstitutionId(@Param("institutionId") institutionId: UUID): List<UserModel>

        @Query(
            """
            SELECT u
            FROM UserModel u
            WHERE u.institution.id = :institutionId
                AND u.isDeleted = false
                AND u.id <> :currentUserId
                AND NOT EXISTS (
                    SELECT 1
                    FROM u.roles r
                    WHERE r.name = 'User'
                )
            """
        )
        fun findAdminUsersByInstitutionIdExcludingCurrentUser(
            @Param("institutionId") institutionId: UUID,
            @Param("currentUserId") currentUserId: UUID
        ): List<UserModel>

    @Query(
        """
        SELECT u
        FROM UserModel u
        WHERE u.institution.id = :institutionId
            AND u.isDeleted = false
            AND EXISTS (
                SELECT 1
                FROM u.roles r
                WHERE r.name = 'User'
            )
            AND NOT EXISTS (
                SELECT 1
                FROM u.roles r2
                WHERE r2.name <> 'User'
            )
            AND (:emailPattern IS NULL OR LOWER(u.email) LIKE :emailPattern)
            AND (:firstNamePattern IS NULL OR LOWER(COALESCE(u.firstName, '')) LIKE :firstNamePattern)
            AND (:lastNamePattern IS NULL OR LOWER(COALESCE(u.lastName, '')) LIKE :lastNamePattern)
            AND (:gender IS NULL OR u.gender = :gender)
            AND (:verifiedStatus IS NULL OR u.isAccountVerified = :verifiedStatus)
        """
    )
    fun findNonAdminUsersByInstitutionIdAndFilters(
        @Param("institutionId") institutionId: UUID,
        @Param("emailPattern") emailPattern: String?,
        @Param("firstNamePattern") firstNamePattern: String?,
        @Param("lastNamePattern") lastNamePattern: String?,
        @Param("gender") gender: Gender?,
        @Param("verifiedStatus") verifiedStatus: Boolean?,
        pageRequest: PageRequest
    ): Page<UserModel>

    @Query(
        """
        SELECT COUNT(u) FROM UserModel u
        WHERE u.institution.id = :institutionId
          AND u.isDeleted = false
          AND EXISTS (SELECT 1 FROM u.roles r WHERE r.name = 'User')
          AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.name <> 'User')
        """
    )
    fun countNonAdminUsersByInstitutionId(@Param("institutionId") institutionId: UUID): Long

    @Query(
        """
        SELECT COUNT(u) FROM UserModel u
        WHERE u.institution.id = :institutionId
          AND u.isDeleted = false
          AND EXISTS (SELECT 1 FROM u.roles r WHERE r.name = 'User')
          AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.name <> 'User')
          AND u.createdAt >= :from AND u.createdAt < :to
        """
    )
    fun countNewNonAdminUsersBetween(
        @Param("institutionId") institutionId: UUID,
        @Param("from") from: java.time.LocalDateTime,
        @Param("to") to: java.time.LocalDateTime
    ): Long

    @Query(
        """
        SELECT u FROM UserModel u
        WHERE u.institution.id = :institutionId
          AND u.isDeleted = false
          AND EXISTS (SELECT 1 FROM u.roles r WHERE r.name = 'User')
          AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.name <> 'User')
        ORDER BY u.createdAt DESC
        """
    )
    fun findRecentNonAdminUsers(
        @Param("institutionId") institutionId: UUID,
        pageable: org.springframework.data.domain.Pageable
    ): List<UserModel>
}
