package com.vicky.docqora.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class RegisterRequestDto {
    private String userName;
    private String email;
    private String password;
}
