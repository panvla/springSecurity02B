package com.vladimirpandurov.securecapita02B.service;

import com.vladimirpandurov.securecapita02B.domain.Role;

public interface RoleService {
    Role getRoleByUserId(Long userId);
}
