package trustline.appuser.model;

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class AuditModel(
    @CreationTimestamp
   open var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
   open var updatedAt: LocalDateTime? = null
) : Serializable
