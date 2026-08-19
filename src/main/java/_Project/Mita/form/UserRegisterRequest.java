package _Project.Mita.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @NotBlank(message = "氏名は必須です")
        @Size(max = 50, message = "氏名は50文字以内で入力してください")
        String name,

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
        String email,

        @NotBlank(message = "パスワードは必須です")
        @Size(min = 8, max = 255, message = "パスワードは8文字以上で入力してください")
        String password) {
}
