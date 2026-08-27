package _Project.Mita.form;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BookRequestTest {

    private Validator validator;
    //Mockと発想は違うが、「道具を用意する」という点では同じ
    //@NotNullのようなアノテーションを実際に読み取って、判定してくれる道具
    //いつもはSpring Bootが勝手に起動している。

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    //テスト用に入力値がすべて正常な有効なインスタンスを生成するヘルパーメソッド
    private BookRequest createValidRequest() {
        return new BookRequest(
                "テスト本",
                "テス太",
                "123456789",
                1L,
                10,
                10,
                "あばばば",
                LocalDate.of(2026, 1, 1)
        );
    }

    @Test
    @DisplayName("正常系：すべてのフィールドが正しく入力されている場合、バリデーションエラーが発生しないこと")
    void success_whenAllFieldsAreValid() {
        BookRequest request = createValidRequest();
        
        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);
        
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("必須系：タイトルがnullの場合、エラーメッセージが返ること")
    void error_whenTitleIsNull() {
        // title以外は正常な値を入れる
        BookRequest request = new BookRequest(
                null, "著者", "ISBN", 1L, 10, 10, "説明", LocalDate.now()
        );

        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<BookRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("タイトルは必須です");
    }

    @Test
    @DisplayName("必須系：タイトルが空文字の場合、エラーメッセージが返ること")
    void error_whenTitleIsEmpty() {
        BookRequest request = new BookRequest(
                "", "著者", "ISBN", 1L, 10, 10, "説明", LocalDate.now()
        );

        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<BookRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("タイトルは必須です");
    }

    @Test
    @DisplayName("必須系：総冊数がnullの場合、エラーメッセージが返ること")
    void error_whenTotalCopiesIsNull() {
        BookRequest request = new BookRequest(
                "タイトル", "著者", "ISBN", 1L, null, 10, "説明", LocalDate.now()
        );

        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<BookRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("総冊数は必須です");
    }

    @Test
    @DisplayName("必須系：貸出可能冊数がnullの場合、エラーメッセージが返ること")
    void error_whenAvailableCopiesIsNull() {
        BookRequest request = new BookRequest(
                "タイトル", "著者", "ISBN", 1L, 10, null, "説明", LocalDate.now()
        );

        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<BookRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("貸出可能冊数は必須です");
    }

    @Test
    @DisplayName("境界値系：総冊数がマイナス値の場合、エラーメッセージが返ること")
    void error_whenTotalCopiesIsNegative() {
        BookRequest request = new BookRequest(
                "タイトル", "著者", "ISBN", 1L, -1, 10, "説明", LocalDate.now()
        );

        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<BookRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("総冊数は0以上で入力してください");
    }
    
    @Test
    @DisplayName("境界値系：総冊数がちょうど0の場合、バリデーションエラーが発生しないこと")
    void success_whenTotalCopiesIsZero() {
        BookRequest request = new BookRequest(
                "タイトル", "著者", "ISBN", 1L, 0, 10, "説明", LocalDate.now()
        );
        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("境界値系：貸出可能冊数がマイナス値の場合、エラーメッセージが返ること")
    void error_whenAvailableCopiesIsNegative() {
        BookRequest request = new BookRequest(
                "タイトル", "著者", "ISBN", 1L, 10, -1, "説明", LocalDate.now()
        );

        Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<BookRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("貸出可能冊数は0以上で入力してください");
    }
}
