package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.RegisterUserDto;
import com.trustline.trustline.appuser.model.User;

public interface UserService {
    User createUser(RegisterUserDto user);
}
