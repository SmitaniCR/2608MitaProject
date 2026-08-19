package _Project.Mita.form;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(

        @NotNull(message = "書籍IDは必須です")
        Long bookId) {
}
