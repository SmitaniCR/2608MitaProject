package _Project.Mita.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    void findAll_削除されていないユーザー一覧を取得できる() {
        // 準備
        List<User> users = List.of(new User(), new User());
        when(userRepository.findByIsDeletedFalse()).thenReturn(users);

        // 実行
        List<User> result = userService.findAll();

        // 検証
        assertThat(result).hasSize(2);
        verify(userRepository, times(1)).findByIsDeletedFalse();
    }
    
    @Test
    void findByIdOptional_削除されていないユーザーが存在すれば取得できる() {
        // 準備
        User user = new User();
        user.setUserId(1L);
        user.setDeleted(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // 実行
        Optional<User> result = userService.findByIdOptional(1L);

        // 検証
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
    }
    
    @Test
    void findByIdOptional_ユーザーが存在しても削除済みの場合は空のOptionalを返す() {
        // 準備
        User user = new User();
        user.setUserId(1L);
        user.setDeleted(true); // 削除済み
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // 実行
        Optional<User> result = userService.findByIdOptional(1L);

        // 検証
        assertThat(result).isEmpty();
    }
    
    @Test
    void findByIdOptional_ユーザーがそもそも存在しない場合は空のOptionalを返す() {
        // 準備
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // 実行
        Optional<User> result = userService.findByIdOptional(99L);

        // 検証
        assertThat(result).isEmpty();
    }
    
    @Test
    void delete_存在するユーザーを論理削除できる() {
        // 準備
        User user = new User();
        user.setUserId(1L);
        user.setDeleted(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 実行
        userService.delete(1L);

        // 検証
        assertThat(user.isDeleted()).isTrue(); // 論理削除フラグが立っていること
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    void delete_存在しないユーザーを削除しようとするとNoSuchElementException() {
        // 準備
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // 実行 & 検証
        assertThrows(NoSuchElementException.class, () -> userService.delete(99L));
        verify(userRepository, never()).save(any(User.class));
    }
    
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
    void register_保存時にDataIntegrityViolationExceptionが発生するとDuplicateEmailExceptionに変換される() {
        UserRegisterRequest request = new UserRegisterRequest("利用者A", "user@example.com", "password123");
        when(userRepository.existsByEmailAndIsDeletedFalse(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(DuplicateEmailException.class, () -> userService.register(request));
    }

    @Test
    void updateRole_対象ユーザーが存在しない場合はNoSuchElementException() {
        // 準備
        User admin = new User();
        admin.setUserId(1L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // 実行 & 検証
        assertThrows(NoSuchElementException.class, 
                () -> userService.updateRole(admin, 99L, true));
        verify(userRepository, never()).save(any(User.class));
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
    void updateRole_自分自身であっても管理者権限を付与する方向であれば更新できる() {
        // 準備
        User admin = new User();
        admin.setUserId(1L);
        admin.setAdmin(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 実行 (isAdmin=true を指定)
        User result = userService.updateRole(admin, admin.getUserId(), true);

        // 検証 (例外にならず、trueのまま維持されること)
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
