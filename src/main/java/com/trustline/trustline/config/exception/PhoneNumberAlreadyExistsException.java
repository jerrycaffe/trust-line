package com.trustline.trustline.config.exception;

public class PhoneNumberAlreadyExistsException extends DuplicateException{
    public PhoneNumberAlreadyExistsException(String phoneNumber){
        super(String.format("User with the Phone number: %s already exist", phoneNumber));
    }
}

