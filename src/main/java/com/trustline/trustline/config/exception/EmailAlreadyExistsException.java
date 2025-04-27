package com.trustline.trustline.config.exception;

public class EmailAlreadyExistsException extends DuplicateException{
    public EmailAlreadyExistsException(String email){
        super(String.format("User with the email: %s already exist", email));
    }
}
