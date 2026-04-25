package trustline.appuser.model;

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime

/**
 * Base class for all auditable JPA entities.
 *
 * Automatically populates [createdAt] on first persist and [updatedAt] on every
 * subsequent save using Hibernate's @CreationTimestamp / @UpdateTimestamp.
 * Implements [Serializable] so entities can be cached or serialised safely.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class AuditModel(
    @CreationTimestamp
   open var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
   open var updatedAt: LocalDateTime? = null
) : Serializable
