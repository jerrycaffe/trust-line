package com.trustline.trustline.config.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class EmailNotFoundException extends UsernameNotFoundException {
    public EmailNotFoundException(String email){
        super(String.format("User with the email: %s does not exist", email));
    }
}
