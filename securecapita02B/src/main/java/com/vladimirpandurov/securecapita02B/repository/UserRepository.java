package com.vladimirpandurov.securecapita02B.repository;

import com.vladimirpandurov.securecapita02B.domain.User;
import com.vladimirpandurov.securecapita02B.dto.UserDTO;

import java.util.Collection;

public interface UserRepository<T extends User>{
    /*Basic CRUD Operations */
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    T getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);

    T verifyCode(String email, String code);
}
