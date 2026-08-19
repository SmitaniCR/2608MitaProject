package _Project.Mita.controller.api;

import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.entity.Loan;
import _Project.Mita.entity.User;
import _Project.Mita.form.LoanRequest;
import _Project.Mita.response.AdminLoanResponse;
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

    @GetMapping
    public List<AdminLoanResponse> list(
            @RequestParam(value = "overdueOnly", required = false, defaultValue = "false") boolean overdueOnly) {
        return loanService.findAllForAdmin(overdueOnly).stream()
                .map(AdminLoanResponse::from)
                .toList();
    }

    @PostMapping("/{id}/admin-return")
    public LoanResponse adminReturn(@PathVariable("id") Long id) {
        return LoanResponse.from(loanService.adminReturnBook(id));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder csv = new StringBuilder("貸出ID,書籍タイトル,利用者名,貸出日,返却期限,返却日,延滞フラグ\r\n");
        LocalDate today = LocalDate.now();
        for (Loan loan : loanService.findAllForAdmin(false)) {
            boolean overdue = loan.getReturnDate() == null && loan.getDueDate().isBefore(today);
            csv.append(CsvUtils.escape(loan.getLoanId())).append(',')
                    .append(CsvUtils.escape(loan.getBook().getTitle())).append(',')
                    .append(CsvUtils.escape(loan.getUser().getName())).append(',')
                    .append(CsvUtils.escape(loan.getLoanDate())).append(',')
                    .append(CsvUtils.escape(loan.getDueDate())).append(',')
                    .append(CsvUtils.escape(loan.getReturnDate())).append(',')
                    .append(CsvUtils.escape(overdue ? "延滞" : "")).append("\r\n");
        }
        return CsvUtils.buildResponse("loans.csv", csv.toString());
    }
}
