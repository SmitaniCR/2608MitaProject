package _Project.Mita.service;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import _Project.Mita.entity.User;
import _Project.Mita.exception.NotAuthenticatedException;

@Service
public class SessionUserService {

    public static final String SESSION_KEY_LOGIN_USER_ID = "loginUserId";

    private final UserService userService;

    public SessionUserService(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> findCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Long userId = (Long) session.getAttribute(SESSION_KEY_LOGIN_USER_ID);
        if (userId == null) {
            return Optional.empty();
        }
        return userService.findByIdOptional(userId);
    }

    public User requireCurrentUser(HttpServletRequest request) {
        return findCurrentUser(request)
                .orElseThrow(() -> new NotAuthenticatedException("ログインが必要です"));
    }
}
