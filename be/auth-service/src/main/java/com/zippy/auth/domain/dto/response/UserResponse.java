package com.zippy.auth.domain.dto.response;
import com.zippy.auth.domain.entity.User;
public record UserResponse(String email, String fullName) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getEmail(), user.getFullName());
    }
}
