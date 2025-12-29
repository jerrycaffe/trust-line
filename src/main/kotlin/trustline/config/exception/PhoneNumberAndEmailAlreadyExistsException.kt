package trustline.config.exception;

class PhoneNumberAndEmailAlreadyExistsException(
    phoneNumber: String,
    email: String
) : DuplicateException(
    "User with the phone number: $phoneNumber and email: $email already exists"
)

