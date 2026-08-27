package _Project.Mita.form;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReservationRequestTest {

    private Validator validator;
    //Mockと発想は違うが、「道具を用意する」という点では同じ
    //@NotNullのようなアノテーションを実際に読み取って、判定してくれる道具
    //いつもはSpring Bootが勝手に起動している。

    @BeforeEach//各テストメソッドの前にこれを実行
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("正常系：bookIdに値が入っている場合、バリデーションエラーが発生しないこと")
    void success_whenBookIdIsPresent() {
    	ReservationRequest reservationRequest = new ReservationRequest(1L);
        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(reservationRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Null系：bookIdがnullの場合、エラーメッセージが返ること")
    void error_whenBookIdIsNull() {
    	ReservationRequest reservationRequest = new ReservationRequest(null);
        Set<ConstraintViolation<ReservationRequest>> violations = validator.validate(reservationRequest);
        assertThat(violations).hasSize(1);
        ConstraintViolation<ReservationRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("書籍IDは必須です");
    }
}
