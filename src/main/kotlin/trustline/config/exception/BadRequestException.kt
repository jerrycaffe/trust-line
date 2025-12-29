package trustline.config.exception;


class BadRequestException : AbstractException {

    constructor(message: String) : super(ErrorCodes.FORMAT_ERROR_CODE, message)

    constructor(code: String, message: String) : super(code, message)
}

