package com.trustline.trustline.controller;

import com.trustline.trustline.dto.RegisterUserDto;
import com.trustline.trustline.dto.UserResponseDto;
import com.trustline.trustline.model.User;
import com.trustline.trustline.repository.UserRepository;
import com.trustline.trustline.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class UserController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;




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
                        Model model){
        User user = userRepository.findByUsername(username);

        if (user != null && passwordEncoder.matches(password, user.getPassword())){
            return "welcome";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "sign-in";
        }
    }

}
