package _Project.Mita.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.entity.User;
import _Project.Mita.form.LoginRequest;
import _Project.Mita.form.UserRegisterRequest;
import _Project.Mita.response.UserResponse;
import _Project.Mita.service.SessionUserService;
import _Project.Mita.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserService userService;
    private final SessionUserService sessionUserService;

    public AuthApiController(UserService userService, SessionUserService sessionUserService) {
        this.userService = userService;
        this.sessionUserService = sessionUserService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return UserResponse.from(userService.register(request));
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        User user = userService.authenticate(request);
        session.setAttribute(SessionUserService.SESSION_KEY_LOGIN_USER_ID, user.getUserId());
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpServletRequest request) {
        return sessionUserService.findCurrentUser(request)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
