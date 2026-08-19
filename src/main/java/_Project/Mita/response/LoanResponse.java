package _Project.Mita.response;

import java.time.LocalDate;

import _Project.Mita.entity.Loan;

public record LoanResponse(
        Long loanId,
        Long bookId,
        String bookTitle,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate) {

    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getLoanId(),
                loan.getBook().getBookId(),
                loan.getBook().getTitle(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate());
    }
}
