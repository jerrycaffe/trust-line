package trustline.config.exception;

class PhoneNumberAlreadyExistsException(phoneNumber: String) : DuplicateException(
    "User with the phone number: $phoneNumber already exists"
)


