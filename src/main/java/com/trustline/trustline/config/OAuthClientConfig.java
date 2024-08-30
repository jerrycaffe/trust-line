package com.trustline.trustline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuthClientConfig {
    @Value("$security.oauth2.client.registration.google.client-id")
    private String clientId;

    @Value("$security.oauth2.client.registration.google.client-secret")
    private String secret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // Creating ClientRegistration objects for each client we want to register
        ClientRegistration googleRegistration = ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(secret)
                .scope("profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .clientName("Google")
                .redirectUri("http://localhost:8082/login/oauth2/code/google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        return new InMemoryClientRegistrationRepository(googleRegistration);
    }
}
