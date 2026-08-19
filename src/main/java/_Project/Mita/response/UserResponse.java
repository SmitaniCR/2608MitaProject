package _Project.Mita.response;

import _Project.Mita.entity.User;

public record UserResponse(Long userId, String name, String email, boolean isAdmin) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.isAdmin());
    }
}
