package com.trustline.trustline.config.security;

import com.trustline.trustline.appuser.model.User;
import com.trustline.trustline.appuser.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GoogleAuthProvider {

    private final UserRepository userRepository;
    private final JWTConfig jwtConfig;

    public String authenticateWithGoogle(String googleId) {
        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new BadCredentialsException("Invalid Google ID"));
        return jwtConfig.generateToken(user);
    }
}

