package _Project.Mita.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
            JsonErrorWriter.write(response, HttpStatus.UNAUTHORIZED.value(), "ログインが必要です");
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
