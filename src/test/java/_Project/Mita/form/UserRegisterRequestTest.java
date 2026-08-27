package _Project.Mita.form;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UserRegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    private UserRegisterRequest createValidRequest() {
        return new UserRegisterRequest("仮田一郎","1ro@com","pasuwado");
    }

    @Test
    void 正常系すべてのフィールドが正しく入力されている場合バリデーションエラーが発生しないこと() {
        UserRegisterRequest request = createValidRequest();
        
        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        
        assertThat(violations).isEmpty();
    }

    @Nested
    class NameValidation {

        @Test
        void 氏名がnullの場合エラーメッセージが返ること() {
            UserRegisterRequest request = new UserRegisterRequest(
                    null, "1ro@com", "pasuwado"
            );

            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("氏名は必須です");
        }

        @Test
        void 氏名が空文字の場合エラーメッセージが返ること() {
            UserRegisterRequest request = new UserRegisterRequest(
                    "", "1ro@com", "pasuwado"
            );

            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("氏名は必須です");
        }

        @Test
        void 氏名がちょうど50文字の場合バリデーションエラーが発生しないこと() {
            String longName = "あ".repeat(50);
            UserRegisterRequest request = new UserRegisterRequest(
                    longName, "1ro@com", "pasuwado"
            );
            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        void 氏名が51文字の場合エラーメッセージが返ること() {
            String tooLongName = "あ".repeat(51);
            UserRegisterRequest request = new UserRegisterRequest(
                    tooLongName, "1ro@com", "pasuwado"
            );
            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("氏名は50文字以内で入力してください");
        }
    }

    @Nested
    @DisplayName("メールアドレスのバリデーションテスト")
    class EmailValidation {

        @Test
        void アドレスがnullの場合エラーメッセージが返ること() {
            UserRegisterRequest request = new UserRegisterRequest(
                    "仮田一郎", null, "pasuwado"
            );

            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("メールアドレスは必須です");
        }

        @Test
        void アドレスの形式が不正な場合エラーメッセージが返ること() {
            UserRegisterRequest request = new UserRegisterRequest(
                    "仮田一郎", "damedame-email-format", "pasuwado"
            );

            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("メールアドレスの形式が正しくありません");
        }

        @Test
        void アドレスが100文字の場合エラーが発生しないこと() {

            String longEmail = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@aaaaaaaaaaaaaaaaaaaaaaaaaexample.com";
            UserRegisterRequest request = new UserRegisterRequest(
                    "仮田一郎", longEmail, "pasuwado"
            );
            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        void アドレスが101文字の場合エラーメッセージが返ること() {
            String tooLongEmail = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@aaaaaaaaaaaaaaaaaaaaaaaaaexample.com";
            UserRegisterRequest request = new UserRegisterRequest(
                    "山田太郎", tooLongEmail, "pasuwado"
            );

            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("メールアドレスは100文字以内で入力してください");
        }
    }

    @Nested
    @DisplayName("パスワードのバリデーションテスト")
    class PasswordValidation {

        @Test
        void パスワードがnullの場合エラーメッセージが返ること() {
            UserRegisterRequest request = new UserRegisterRequest(
                    "山田太郎", "1ro@com", null
            );
            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("パスワードは必須です");
        }

        @Test
        void パスワードが7文字の場合エラーメッセージが返ること() {
            UserRegisterRequest request = new UserRegisterRequest(
                    "仮田一郎", "1ro@com", "1234567"
            );
            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("パスワードは8文字以上で入力してください");
        }

        @Test
        void パスワードがちょうど8文字の場合エラーが発生しないこと() {
            UserRegisterRequest request = new UserRegisterRequest(
                    "仮田一郎", "1ro@com", "pasuwado"
            );
            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        void パスワードが256文字の場合エラーメッセージが返ること() {
            String tooLongPassword = "p".repeat(256);
            UserRegisterRequest request = new UserRegisterRequest(
                    "仮田一郎", "1ro@com", tooLongPassword
            );

            Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            ConstraintViolation<UserRegisterRequest> violation = violations.iterator().next();
            assertThat(violation.getMessage()).isEqualTo("パスワードは8文字以上で入力してください"); 
        }
    }
}
