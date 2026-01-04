package trustline.config.exception;

open class DuplicateException : AbstractException {

    constructor(message: String) : super("D01", message)

    constructor(code: String, message: String) : super(code, message)
}

