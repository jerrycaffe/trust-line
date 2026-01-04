package trustline.config.exception;

class EmailAlreadyExistsException(email: String) : DuplicateException(
    "User with the email: $email already exists"
)


