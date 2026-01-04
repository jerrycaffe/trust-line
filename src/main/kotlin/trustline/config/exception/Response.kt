package trustline.config.exception;

import com.fasterxml.jackson.annotation.JsonInclude


@JsonInclude(JsonInclude.Include.NON_NULL)
data class Response(
    val error: Error
)

