package com.vicky.docqora.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private UserDto userDto;
}
