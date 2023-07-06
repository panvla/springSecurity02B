package com.vladimirpandurov.securecapita02B.repository.implementation;

import com.vladimirpandurov.securecapita02B.domain.User;
import com.vladimirpandurov.securecapita02B.domain.UserPrincipal;
import com.vladimirpandurov.securecapita02B.dto.UserDTO;
import com.vladimirpandurov.securecapita02B.exception.ApiException;
import com.vladimirpandurov.securecapita02B.repository.RoleRepository;
import com.vladimirpandurov.securecapita02B.repository.UserRepository;
import com.vladimirpandurov.securecapita02B.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

import static com.vladimirpandurov.securecapita02B.enumeration.RoleType.ROLE_USER;
import static com.vladimirpandurov.securecapita02B.enumeration.VerificationType.ACCOUNT;
import static com.vladimirpandurov.securecapita02B.query.UserQuery.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {


    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private static final String DATA_FORMAT = "yyyy-MM-dd hh:mm:ss";

    @Override
    public User create(User user) {
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new ApiException("Email already in use.Please use a different email and try again");
        try{
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY, parameters, holder);
            user.setId(Objects.requireNonNull(holder.getKey()).longValue());
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));
            user.setEnabled(true);
            user.setNotLocked(true);
            return user;
        }catch (Exception exception){
            throw new ApiException("An error occurred.USER_REPOSITORY_CREATE()");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if(user == null){
            throw new UsernameNotFoundException("User not found in the database");
        }else {
            UserPrincipal userPrincipal = new UserPrincipal(user, this.roleRepository.getRoleByUserEmail(email));
            return userPrincipal;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try{
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            return user;
        }catch (EmptyResultDataAccessException exception){
            throw new ApiException("No user found by email: " + email);
        }catch (Exception exception){
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String expirationDate = format(addDays(new Date(), 1), DATA_FORMAT);
        String verificationCode = randomAlphabetic(8).toUpperCase();
        try{
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, Map.of("id", user.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, Map.of("user_id", user.getId(),
                    "code", verificationCode, "expirationDate", expirationDate));
            log.info("Phone number : {}, verification code: {}", user.getPhone(), verificationCode);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    public User verifyCode(String email, String code) {
        if(isVerificatoinCodeExpired(code)) throw new ApiException("This code has expired. Please login again.");
        try{
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, Map.of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
            if(userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())){
                jdbc.update(DELETE_CODE_BY_CODE_QUERY, Map.of("code", code));
                return userByCode;
            }else{
                throw new ApiException("Code is invalid. Please try again.");
            }
        }catch (EmptyResultDataAccessException exception){
            throw new ApiException("Unable to find record");
        }catch (Exception exception){
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }



    private Integer getEmailCount(String email){
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
    }

    private Boolean isVerificatoinCodeExpired(String code) {
        try{
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, Map.of("code", code), Boolean.class);
        }catch (EmptyResultDataAccessException exception){
            throw new ApiException("This code is not valid. Please login again.");
        }catch (Exception exception){
            log.info(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }


}
