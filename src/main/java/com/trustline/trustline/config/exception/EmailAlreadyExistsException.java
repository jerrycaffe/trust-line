package com.trustline.trustline.config.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class EmailAlreadyExistsException extends DuplicateException{
    public EmailAlreadyExistsException(String email){
        super(String.format("User with the email: %s already exist", email));
    }
}

