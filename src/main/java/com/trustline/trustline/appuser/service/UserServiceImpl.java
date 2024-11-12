package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.RegisterUserDto;
import com.trustline.trustline.appuser.model.AuthProvider;
import com.trustline.trustline.appuser.model.User;
import com.trustline.trustline.appuser.repository.UserRepository;
import com.trustline.trustline.config.exception.DuplicateException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User createUser(RegisterUserDto user) {

        Optional<User> prevUser = userRepository.findByEmail(user.getEmail());
        if (prevUser.isPresent() || userRepository.existsByPhoneNumber(user.getPhoneNumber()))
            throw new DuplicateException(String.format("Account with email: %s or phone: %s already exists", user.getEmail(), user.getPhoneNumber()));

User newUser = User.builder()
        .email(user.getEmail())
        .authProvider(AuthProvider.LOCAL)
        .password(passwordEncoder.encode(user.getPassword()))
        .accountVerified(false)
        .phoneNumber(user.getPhoneNumber())
        .build();

//Generate Token to be sent to the phone number
        //Ensure user verifies account
        return userRepository.save(newUser);
    }


}
