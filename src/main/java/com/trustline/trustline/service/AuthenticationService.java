package com.trustline.trustline.service;

import com.trustline.trustline.dto.RegisterUserDto;
import com.trustline.trustline.model.User;
import com.trustline.trustline.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

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

    public String login(String username, String password, Model model){
        User user = userRepository.findByUsername(username);

        if (user != null && passwordEncoder.matches(password, user.getPassword())){
            return "welcome";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "sign-in";
        }
    }

}