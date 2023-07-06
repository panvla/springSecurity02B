package com.vladimirpandurov.securecapita02B.repository;

import com.vladimirpandurov.securecapita02B.domain.Role;

import java.util.Collection;

public interface RoleRepository<T extends Role> {
    /*Basic CRUD Operations*/
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    /*More Complex Operations */
    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);
}
