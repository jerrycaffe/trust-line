package trustline.config.exception;


class RestClientException : AbstractException {

    var additionalInfo: Map<String, Any> = emptyMap()
        private set

    constructor(message: String) : super(REST_CLIENT_ERROR_CODE, message) {
        additionalInfo = emptyMap()
    }

    constructor(message: String, additionalInfo: Map<String, Any>) : super(REST_CLIENT_ERROR_CODE, message) {
        this.additionalInfo = additionalInfo
    }

    constructor(code: String, message: String) : super(code, message) {
        additionalInfo = emptyMap()
    }

    companion object {
        const val REST_CLIENT_ERROR_CODE = "101"
    }
}
