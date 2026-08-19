package _Project.Mita.form;

import jakarta.validation.constraints.NotNull;

public record LoanRequest(

        @NotNull(message = "書籍IDは必須です")
        Long bookId) {
}
