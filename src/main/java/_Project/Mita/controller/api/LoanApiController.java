package _Project.Mita.controller.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.entity.User;
import _Project.Mita.form.LoanRequest;
import _Project.Mita.response.LoanResponse;
import _Project.Mita.service.LoanService;
import _Project.Mita.service.SessionUserService;

@RestController
@RequestMapping("/api/loans")
public class LoanApiController {

    private final LoanService loanService;
    private final SessionUserService sessionUserService;

    public LoanApiController(LoanService loanService, SessionUserService sessionUserService) {
        this.loanService = loanService;
        this.sessionUserService = sessionUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse create(@Valid @RequestBody LoanRequest request, HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return LoanResponse.from(loanService.create(user, request));
    }

    @PostMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return LoanResponse.from(loanService.returnBook(user, id));
    }

    @GetMapping("/me")
    public List<LoanResponse> myLoans(HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return loanService.findMyLoans(user).stream()
                .map(LoanResponse::from)
                .toList();
    }
}
