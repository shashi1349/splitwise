package com.shashi.splitwise.auth.api.dto;

import com.shashi.splitwise.user.domain.User;

public record UserDto(Long id, String email, String displayName) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
