package trustline.config.exception;



class NotFoundException : AbstractException {

    constructor(message: String) : super(ErrorCodes.NOT_FOUND_ERROR_CODE, message)

    constructor(code: String, message: String) : super(code, message)
}

