package trustline.appuser.dto

import java.util.*


data class EmailRequest (
     val recipientName: String? = null,
     val recipientEmail: String? = null,
     val recipientId: UUID? = null,
     val htmlTemplate: String? = null,
     val subject: String? = null
)