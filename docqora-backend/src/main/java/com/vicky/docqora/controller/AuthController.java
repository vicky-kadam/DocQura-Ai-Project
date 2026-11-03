package com.vicky.docqora.controller;

import com.vicky.docqora.Service.UserService;
import com.vicky.docqora.dto.LoginRequestDto;
import com.vicky.docqora.dto.LoginResponse;
import com.vicky.docqora.dto.RegisterRequestDto;
import com.vicky.docqora.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequestDto loginRequest){
        LoginResponse response = userService.loginUser(loginRequest);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequestDto registerRequestDto){
        UserDto userDto = userService.registerUser(registerRequestDto);
        return ResponseEntity.ok(userDto);

    }
}
