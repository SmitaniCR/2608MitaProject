package _Project.Mita.form;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequest(

        @NotBlank(message = "タイトルは必須です")
        @Size(max = 255, message = "タイトルは255文字以内で入力してください")
        String title,

        @Size(max = 100, message = "著者名は100文字以内で入力してください")
        String author,

        @Size(max = 20, message = "ISBNは20文字以内で入力してください")
        String isbn,

        Long categoryId,

        @NotNull(message = "総冊数は必須です")
        @Min(value = 0, message = "総冊数は0以上で入力してください")
        Integer totalCopies,

        @NotNull(message = "貸出可能冊数は必須です")
        @Min(value = 0, message = "貸出可能冊数は0以上で入力してください")
        Integer availableCopies,

        String description,

        LocalDate publishedDate) {
}
