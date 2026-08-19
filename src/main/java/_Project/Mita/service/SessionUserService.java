package _Project.Mita.service;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import _Project.Mita.entity.User;
import _Project.Mita.exception.NotAuthenticatedException;
import _Project.Mita.security.UserPrincipal;

@Service
public class SessionUserService {

    public Optional<User> findCurrentUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal.getUser());
    }

    public User requireCurrentUser(HttpServletRequest request) {
        return findCurrentUser(request)
                .orElseThrow(() -> new NotAuthenticatedException("ログインが必要です"));
    }
}
