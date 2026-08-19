package _Project.Mita.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "カテゴリ名は必須です")
        @Size(max = 50, message = "カテゴリ名は50文字以内で入力してください")
        String categoryName) {
}
