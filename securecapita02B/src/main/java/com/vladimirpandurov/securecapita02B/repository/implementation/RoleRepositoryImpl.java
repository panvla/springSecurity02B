package com.vladimirpandurov.securecapita02B.repository.implementation;

import com.vladimirpandurov.securecapita02B.domain.Role;
import com.vladimirpandurov.securecapita02B.exception.ApiException;
import com.vladimirpandurov.securecapita02B.repository.RoleRepository;
import com.vladimirpandurov.securecapita02B.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.vladimirpandurov.securecapita02B.query.RoleQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Role create(Role data) {
        return null;
    }

    @Override
    public Collection list(int page, int pageSize) {
        return null;
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        //log.info("Adding role {} to user id: {}", roleName, userId);
        try{
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName", roleName), new RoleRowMapper());
            jdbc.update(INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", Objects.requireNonNull(role).getId()));
        }catch (EmptyResultDataAccessException exception){
            throw new ApiException("No role found by name: " + roleName);
        }catch (Exception exception){
            throw new ApiException("An error occurred.ROLE_REPOSITORY_ADD_ROLE_TO_USER");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        log.info("Fetching role  for user id: {}", userId);
        try{
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_ID_QUERY, Map.of("user_id", userId), new RoleRowMapper());
            log.info("Permissions for this user : " + role.getPermission());
            return role;

        }catch(EmptyResultDataAccessException exception){
            throw new ApiException("No role found by id: " + userId);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred.Please try again.");
        }

    }

    @Override
    public Role getRoleByUserEmail(String email) {

        try{
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_USER_EMAIL_QUERY, Map.of("email", email), new RoleRowMapper());
            return role;
        }catch (EmptyResultDataAccessException exception){
            throw new ApiException("No role found by user email: " + email);
        }catch (Exception exception){
            throw new ApiException("An error occurred in getRoleByUserId in RoleRepository, Please try again later.");
        }

    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
