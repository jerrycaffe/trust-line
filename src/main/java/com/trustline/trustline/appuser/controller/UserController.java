package com.trustline.trustline.appuser.controller;

import com.trustline.trustline.appuser.dto.RegisterUserDto;
import com.trustline.trustline.appuser.dto.UserResponseDto;
import com.trustline.trustline.appuser.model.User;
import com.trustline.trustline.appuser.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@AllArgsConstructor
@RestController
@RequestMapping("api/v1/auth")
public class UserController {

private final UserService userService;



    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Validated RegisterUserDto registerUserDto) {
      return UserResponseDto.fromUser(userService.createUser(registerUserDto));
    }

//    @RequestMapping("/login")
//    public String login(@RequestParam("username") String username,
//                        @RequestParam("password") String password,
//                        Model model) {
//        return authenticationService.login(username, password, model);
//    }

}
