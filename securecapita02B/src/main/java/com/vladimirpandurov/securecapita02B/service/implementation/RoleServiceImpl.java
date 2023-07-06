package com.vladimirpandurov.securecapita02B.service.implementation;

import com.vladimirpandurov.securecapita02B.domain.Role;
import com.vladimirpandurov.securecapita02B.repository.RoleRepository;
import com.vladimirpandurov.securecapita02B.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getRoleByUserId(Long userId) {
        return this.roleRepository.getRoleByUserId(userId);
    }
}
