package com.trustline.trustline.appuser.controller;


import com.trustline.trustline.appuser.dto.OtpRequest;
import com.trustline.trustline.appuser.dto.RegisterUserDto;
import com.trustline.trustline.appuser.dto.UserResponseDto;
import com.trustline.trustline.appuser.model.User;
import com.trustline.trustline.appuser.service.UserService;
import com.trustline.trustline.config.firebase.FirebaseConfig;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;
@AllArgsConstructor
@RestController
@RequestMapping("api/v1/auth")
public class UserController {

private final UserService userService;
private final FirebaseConfig firebaseConfig;



    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Validated RegisterUserDto registerUserDto) {
      return UserResponseDto.fromUser(userService.createUser(registerUserDto));
    }

//    @GetMapping("/verify-otp")
//            public String verifyOtp(
//                    @Validated @RequestBody OtpRequest otpRequest
//    ){
//
//    }



//    @RequestMapping("/login")
//    public String login(@RequestParam("username") String username,
//                        @RequestParam("password") String password,
//                        Model model) {
//        return authenticationService.login(username, password, model);
//    }

}
