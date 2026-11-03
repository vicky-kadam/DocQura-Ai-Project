package com.vicky.docqora.mapper;

import com.vicky.docqora.dto.UserDto;
import com.vicky.docqora.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toUserDto(User user){
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        return userDto;

    }
}
