package trustline.config.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import trustline.appuser.model.User;
import trustline.appuser.repository.UserRepository;

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

