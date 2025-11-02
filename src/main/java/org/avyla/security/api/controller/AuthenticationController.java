package org.avyla.security.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.avyla.security.api.dto.AuthCreateUserRequest;
import org.avyla.security.api.dto.AuthLoginRequest;
import org.avyla.security.api.dto.AuthResponse;
import org.avyla.security.application.service.UserDetailServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController
{
    private final UserDetailServiceImpl userDetailService;

    @PostMapping("/sing-up")
    public ResponseEntity<AuthResponse> registerUser
            (
                    @RequestBody
                    @Valid
                    AuthCreateUserRequest authRequest
            )
    {
        return new ResponseEntity<>(this.userDetailService.createUser(authRequest), HttpStatus.CREATED);
    }

    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> login
            (
                    @RequestBody
                    @Valid
                    AuthLoginRequest authRequest
            )
    {
        return new ResponseEntity<>(this.userDetailService.loginUser(authRequest), HttpStatus.OK);
    }

}
