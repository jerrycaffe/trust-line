package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.RegisterUserDto;
import com.trustline.trustline.appuser.model.Status;
import com.trustline.trustline.appuser.model.User;
import com.trustline.trustline.appuser.repository.UserRepository;
import com.trustline.trustline.config.exception.DuplicateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;



    RegisterUserDto registerReq() {
        return RegisterUserDto.builder()
                .email("test@test.com")
                .password("test1234")
                .phoneNumber("08088492993")
                .build();
    }

    User dbUser() {
        return User.builder()
                .email("test@test.com")
                .phoneNumber("08088492993")
                .status(Status.OTP_VALIDATION)
                .id(UUID.randomUUID())
                .accountVerified(false)
                .build();
    }

    @Test
    void createUserShouldRaiseExceptionWhenUserExistByEmail() throws DuplicateException {
        RegisterUserDto registerUserReq = registerReq();

        User user = dbUser();

        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(user));

        var exception = assertThrows(DuplicateException.class, () -> userService.createUser(registerUserReq));

        assertEquals(String.format("Account with email: %s or phone: %s already exists", registerUserReq.getEmail(), registerUserReq.getPhoneNumber()), exception.getMessage());
        verify(userRepository, never()).save(user);
    }


    @Test
    void createUserShouldReturnSuccess() {
        User newUser = dbUser();
        RegisterUserDto registerUserDto = registerReq();
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User response = userService.createUser(registerUserDto);
        assertEquals(Status.OTP_VALIDATION, response.getStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUserShouldReturnSuccessForExistingUser(){
        User newUser = dbUser();
        RegisterUserDto registerUserDto = registerReq();
        when(userRepository.findByEmailOrPhoneNumber(registerReq().getEmail(), registerReq().getPhoneNumber())).thenReturn(Optional.of(newUser));

        User response = userService.createUser(registerUserDto);
        assertEquals(Status.OTP_VALIDATION, response.getStatus());
        verify(userRepository,never()).save(any(User.class));
    }
}