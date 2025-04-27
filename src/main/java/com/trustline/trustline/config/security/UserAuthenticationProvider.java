package com.trustline.trustline.config.security;

import com.trustline.trustline.appuser.model.User;
import com.trustline.trustline.appuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@RequiredArgsConstructor
@Component
public class UserAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        if(!passwordEncoder.matches(password, user.getPassword())) throw new BadCredentialsException("Invalid Credentials");
        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority(user.getRoles().toString())));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return  UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
