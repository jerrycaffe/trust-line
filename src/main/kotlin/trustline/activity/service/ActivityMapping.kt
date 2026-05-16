package trustline.activity.service

import trustline.activity.dto.*
import trustline.activity.model.ActivityModel
import trustline.activity.model.GradeType
import trustline.activity.model.UserActivityEntryModel
import java.math.BigDecimal
import java.time.LocalDateTime

internal fun ActivityModel.toResponse(): ActivityResponseDto = ActivityResponseDto(
    id = this.id!!,
    name = this.name,
    description = this.description,
    gradeType = this.gradeType,
    unit = this.gradeType.unitLabel(),
    createdById = this.createdBy.id!!,
    createdByName = listOfNotNull(this.createdBy.firstName, this.createdBy.lastName)
        .joinToString(" ").ifBlank { this.createdBy.email ?: "" },
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

internal fun UserActivityEntryModel.toResponse(): ActivityEntryResponseDto = ActivityEntryResponseDto(
    id = this.id!!,
    activityId = this.activity.id!!,
    activityName = this.activity.name,
    gradeType = this.activity.gradeType,
    unit = this.activity.gradeType.unitLabel(),
    userId = this.user.id!!,
    userName = listOfNotNull(this.user.firstName, this.user.lastName)
        .joinToString(" ").ifBlank { this.user.email ?: "" },
    value = this.value,
    notes = this.notes,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

internal fun GradeType.unitLabel(): String = when (this) {
    GradeType.PERCENTAGE -> "%"
    GradeType.TIME -> "seconds"
}

internal fun BigDecimal?.orNull(): BigDecimal? = this

internal fun Any?.asLong(): Long = when (this) {
    is Number -> this.toLong()
    null -> 0L
    else -> this.toString().toLongOrNull() ?: 0L
}

internal fun Any?.asBigDecimal(): BigDecimal? = when (this) {
    is BigDecimal -> this
    is Number -> BigDecimal(this.toString())
    null -> null
    else -> this.toString().toBigDecimalOrNull()
}

internal fun Any?.asLocalDateTime(): LocalDateTime? = when (this) {
    is LocalDateTime -> this
    is java.sql.Timestamp -> this.toLocalDateTime()
    null -> null
    else -> null
}
