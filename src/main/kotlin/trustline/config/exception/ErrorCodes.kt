package trustline.config.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
object ErrorCodes {

    const val SERVER_ERROR_CODE = "06"
    const val SERVER_ERROR_MESSAGE = "General Error."

    const val DUPLICATE_ERROR_CODE = "94"
    const val DUPLICATE_ERROR_MESSAGE = "Duplicate Record."

    const val FORMAT_ERROR_CODE = "30"
    const val FORMAT_ERROR_MESSAGE = "Format Error."

    const val NOT_FOUND_ERROR_CODE = "25"
    const val NOT_FOUND_ERROR_MESSAGE = "Not Found."

    const val BAD_CREDENTIALS_ERROR_CODE = "21"

    const val ILLEGAL_INPUT_ERROR_CODE = "76"
    const val ILLEGAL_INPUT_ERROR_MESSAGE = "Improper Argument passed"
}

