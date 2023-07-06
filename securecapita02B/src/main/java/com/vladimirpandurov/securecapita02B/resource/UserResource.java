package com.vladimirpandurov.securecapita02B.resource;

import com.vladimirpandurov.securecapita02B.domain.HttpResponse;
import com.vladimirpandurov.securecapita02B.domain.User;
import com.vladimirpandurov.securecapita02B.domain.UserPrincipal;
import com.vladimirpandurov.securecapita02B.dto.UserDTO;
import com.vladimirpandurov.securecapita02B.dtoMapper.UserDTOMapper;
import com.vladimirpandurov.securecapita02B.form.LoginForm;
import com.vladimirpandurov.securecapita02B.service.RoleService;
import com.vladimirpandurov.securecapita02B.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserResource {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> create(@RequestBody @Valid User user) {
        UserDTO userDTO = this.userService.createUser(user);
        return ResponseEntity.created(getUri(userDTO.getId())).body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(Map.of("user", userDTO))
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .message("User created")
                .build()
        );
    }
    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
        UserDTO user = userService.getUserByEmail(loginForm.getEmail());
        return user.isUsingMfa() ? sendVerificationCode(user) : sendResponse(user);
    }
    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        UserDTO user = userService.verifyCode(email, code);
        return sendResponse(user);
    }
    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDTO user = userService.getUserByEmail(authentication.getName());
        log.info(authentication.getName());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(Map.of("user", user))
                .message("Profile Retrieved")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }

    private URI getUri(Long userId) {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/" + userId).toUriString());
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO user){
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("user", user))
                        .message("Login Success")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO user) {
        this.userService.sendVerificationCode(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(Map.of("user", user))
                .message("Verification code sent")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }

    private UserPrincipal getUserPrincipal(UserDTO user) {
        return new UserPrincipal(UserDTOMapper.toUser(userService.getUserByEmail(user.getEmail())), this.roleService.getRoleByUserId(user.getId()));
    }
}
