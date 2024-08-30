package com.trustline.trustline.controller;

import com.trustline.trustline.dto.RegisterUserDto;
import com.trustline.trustline.dto.UserResponseDto;
import com.trustline.trustline.model.User;
import com.trustline.trustline.repository.UserRepository;
import com.trustline.trustline.service.AuthenticationService;
import com.trustline.trustline.service.TrustLineUserDetailsService;
import com.trustline.trustline.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@AllArgsConstructor
@RestController
@RequestMapping("api/auth")
public class UserController {

    private final AuthenticationService authenticationService;

    private final PasswordEncoder passwordEncoder;

    private final TrustLineUserDetailsService trustLineUserDetailsService;

    private final UserService service;


    @GetMapping("/{id}")
    public User getUserById(@PathVariable UUID id) {
        return trustLineUserDetailsService.getUserById(id);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(registeredUser.getId());
        userResponseDto.setUsername(registeredUser.getUsername());
        return ResponseEntity.ok(userResponseDto);
    }

    @RequestMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model) {
        return authenticationService.login(username, password, model);
    }


    @PostMapping("/login")
    public String login(User user){
        return service.verify(user);
    }

}
