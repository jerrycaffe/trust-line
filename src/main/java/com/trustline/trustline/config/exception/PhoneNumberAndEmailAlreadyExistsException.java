package com.trustline.trustline.config.exception;

public class PhoneNumberAndEmailAlreadyExistsException extends DuplicateException{
    public PhoneNumberAndEmailAlreadyExistsException(String phoneNumber, String email){
        super(String.format("User with the Phone number: %s and Email: %s already exist", phoneNumber, email));
    }
}
