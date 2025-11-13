package com.example.OrdersTracking.config;

import com.example.OrdersTracking.dtos.UserDto;
import com.example.OrdersTracking.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private BCrypt bCrypt;

    public User toEntity(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(bCrypt.passwordEncoder().encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setEnabled(true);
        user.setRole("User");

        return user;
    }

}
