package _Project.Mita.response;

import java.time.LocalDate;

import _Project.Mita.entity.Loan;

public record AdminLoanResponse(
        Long loanId,
        Long bookId,
        String bookTitle,
        Long userId,
        String userName,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        boolean overdue) {

    public static AdminLoanResponse from(Loan loan) {
        boolean overdue = loan.getReturnDate() == null && loan.getDueDate().isBefore(LocalDate.now());
        return new AdminLoanResponse(
                loan.getLoanId(),
                loan.getBook().getBookId(),
                loan.getBook().getTitle(),
                loan.getUser().getUserId(),
                loan.getUser().getName(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                overdue);
    }
}
