package com.vicky.docqora.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserDto{
    private Long id;
    private String userName;
    private String email;
    private String role;
}
