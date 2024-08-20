package com.trustline.trustline.service;

import com.trustline.trustline.dto.LoginUserDto;
import com.trustline.trustline.dto.RegisterUserDto;
import com.trustline.trustline.model.User;
import com.trustline.trustline.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public User signup(RegisterUserDto registerUserDto) {
        User user = new User(
                registerUserDto.getUsername());
        return userRepository.save(user);
    }

//    public User authenticate(LoginUserDto input) {
//        User user = userRepository.findByUsername(input.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        input.getUsername(),
//                        input.getPassword()
//                )
//        );
//        return user;
//
//    }
}