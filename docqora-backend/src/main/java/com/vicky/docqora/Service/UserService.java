package com.vicky.docqora.Service;


import com.vicky.docqora.Service.security.CustomUserDetails;
import com.vicky.docqora.dto.LoginRequestDto;
import com.vicky.docqora.dto.LoginResponse;
import com.vicky.docqora.dto.RegisterRequestDto;
import com.vicky.docqora.dto.UserDto;
import com.vicky.docqora.entity.Role;
import com.vicky.docqora.entity.User;
import com.vicky.docqora.mapper.UserMapper;
import com.vicky.docqora.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public UserDto registerUser(RegisterRequestDto registerRequestDto) {
        User user = User.builder()
                .userName(registerRequestDto.getUserName())
                .email(registerRequestDto.getEmail())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toUserDto(savedUser);


    }

    public LoginResponse loginUser(@Valid LoginRequestDto loginRequest) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.getUserName(),loginRequest.getPassword());
       Authentication authenticated =  authenticationManager.authenticate(authentication);
       User user = userRepository.findByUserName(loginRequest.getUserName()).orElseThrow(() -> new RuntimeException("User not found"));
       String token = jwtService.generateToken(new CustomUserDetails(user));
       return LoginResponse.builder()
               .accessToken(token)
               .userDto(userMapper.toUserDto(user))
               .build();

    }
}
