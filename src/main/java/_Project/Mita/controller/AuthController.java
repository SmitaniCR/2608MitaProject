package _Project.Mita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }
}
