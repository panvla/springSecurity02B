package com.vladimirpandurov.securecapita02B.service;

import com.vladimirpandurov.securecapita02B.domain.User;
import com.vladimirpandurov.securecapita02B.dto.UserDTO;

public interface UserService {

    UserDTO createUser(User user);

    UserDTO getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);

    UserDTO verifyCode(String email, String code);
}
