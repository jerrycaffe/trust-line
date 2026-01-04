package trustline.config.exception;


open class AbstractException(
    var code: String,
    message: String
) : RuntimeException(message) {

    var error: Error = Error(code, message)

}

