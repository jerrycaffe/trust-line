package trustline.config.exception;

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Error(
    var code: String? = null,
    var message: String? = null
)

