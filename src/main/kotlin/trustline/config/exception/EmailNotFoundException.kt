package trustline.config.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException

class EmailNotFoundException(email: String) : UsernameNotFoundException(
    "User with the email: $email does not exist"
)

