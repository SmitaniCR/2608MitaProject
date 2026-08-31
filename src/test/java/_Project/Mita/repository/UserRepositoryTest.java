package _Project.Mita.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import _Project.Mita.entity.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User createUser(String name, String email, boolean isDeleted) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password");
        user.setDeleted(isDeleted); // 論理削除フラグを設定
        return entityManager.persist(user);
    }

    // findByIsDeletedFalse() のテスト

    @Test
    void 削除されていないユーザーのみが全件取得できること() {
        // 1. 準備
        createUser("一郎", "1ro@com", false); // 取得対象
        createUser("二郎", "2ro@com", false); // 取得対象
        createUser("三郎", "3ro@com", true);  // 除外対象

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        List<User> result = userRepository.findByIsDeletedFalse();

        // 3. 検証
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getName).containsExactlyInAnyOrder("一郎", "二郎");
        assertThat(result).extracting(User::isDeleted).containsOnly(false);
    }

    // findByEmailAndIsDeletedFalse(String email) のテスト

    @Test
    void 指定したメールアドレスかつ削除されていないユーザーが取得できること() {
        // 1. 準備
        String targetEmail = "target@com";
        createUser("対象ユーザー", targetEmail, false);

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        Optional<User> result = userRepository.findByEmailAndIsDeletedFalse(targetEmail);

        // 3. 検証
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(targetEmail);
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    void メールアドレスが一致しても削除済みの場合は取得できないこと() {
        // 1. 準備
        String targetEmail = "deleted@com";
        createUser("削除済みユーザー", targetEmail, true); // メールは一致するが削除済み

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        Optional<User> result = userRepository.findByEmailAndIsDeletedFalse(targetEmail);

        // 3. 検証
        assertThat(result).isEmpty();
    }

    // existsByEmailAndIsDeletedFalse(String email) のテスト

    @Test
    void 指定したメールアドレスかつ削除されていないユーザーが存在する場合にtrueを返すこと() {
        // 1. 準備
        String targetEmail = "exist@com";
        createUser("存在ユーザー", targetEmail, false);

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        boolean result = userRepository.existsByEmailAndIsDeletedFalse(targetEmail);

        // 3. 検証
        assertThat(result).isTrue();
    }

    @Test
    void メールアドレスが一致するユーザーが削除済みの場合はfalseを返すこと() {
        // 1. 準備
        String targetEmail = "exist_but_deleted@com";
        createUser("削除済みユーザー", targetEmail, true);

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        boolean result = userRepository.existsByEmailAndIsDeletedFalse(targetEmail);

        // 3. 検証
        assertThat(result).isFalse();
    }

    @Test
    void 指定したメールアドレスのユーザーがそもそも存在しない場合にfalseを返すこと() {
        // 1. 準備
        createUser("別ユーザー", "other@com", false);

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        boolean result = userRepository.existsByEmailAndIsDeletedFalse("notfound@com");

        // 3. 検証
        assertThat(result).isFalse();
    }
}
