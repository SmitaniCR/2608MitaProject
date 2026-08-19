package _Project.Mita.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import _Project.Mita.entity.User;
import _Project.Mita.exception.DuplicateEmailException;
import _Project.Mita.exception.SelfDemotionException;
import _Project.Mita.form.UserRegisterRequest;
import _Project.Mita.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_新規会員登録できる() {
        UserRegisterRequest request = new UserRegisterRequest("利用者A", "user@example.com", "password123");
        when(userRepository.existsByEmailAndIsDeletedFalse(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.register(request);

        assertThat(user.getName()).isEqualTo(request.name());
        assertThat(user.getEmail()).isEqualTo(request.email());
        assertThat(user.getPassword()).isEqualTo("hashed-password");
    }

    @Test
    void register_メールアドレス重複時にDuplicateEmailException() {
        UserRegisterRequest request = new UserRegisterRequest("利用者A", "user@example.com", "password123");
        when(userRepository.existsByEmailAndIsDeletedFalse(request.email())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.register(request));
    }

    @Test
    void updateRole_他ユーザーの権限を昇格できる() {
        User admin = new User();
        admin.setUserId(1L);

        User target = new User();
        target.setUserId(2L);
        target.setAdmin(false);
        when(userRepository.findById(target.getUserId())).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateRole(admin, target.getUserId(), true);

        assertThat(result.isAdmin()).isTrue();
    }

    @Test
    void updateRole_自分自身を降格しようとするとSelfDemotionException() {
        User admin = new User();
        admin.setUserId(1L);
        admin.setAdmin(true);

        assertThrows(SelfDemotionException.class,
                () -> userService.updateRole(admin, admin.getUserId(), false));
    }
}
