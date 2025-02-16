package com.trustline.trustline.config.firebase;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FirebaseConfig {

    public String sendOtp(String phoneNumber) throws FirebaseAuthException {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.createCustomToken(phoneNumber);
    }

    public String verifyPhoneNumber(String phoneNumber, String code){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        PhoneAuthCredential credential = PhoneAuthProvider.getCredentials();
        return null;
    }
}
