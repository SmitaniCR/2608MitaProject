package _Project.Mita.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.User;
import _Project.Mita.exception.DuplicateEmailException;
import _Project.Mita.exception.SelfDemotionException;
import _Project.Mita.form.UserRegisterRequest;
import _Project.Mita.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findByIsDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByIdOptional(Long userId) {
        return userRepository.findById(userId).filter(user -> !user.isDeleted());
    }

    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: id=" + userId));
        user.setDeleted(true);
        userRepository.save(user);
    }

    public User updateRole(User currentAdmin, Long userId, boolean isAdmin) {
        if (!isAdmin && currentAdmin.getUserId().equals(userId)) {
            throw new SelfDemotionException("自分自身の管理者権限は変更できません");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: id=" + userId));
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    public User register(UserRegisterRequest request) {
        if (userRepository.existsByEmailAndIsDeletedFalse(request.email())) {
            throw new DuplicateEmailException("このメールアドレスは既に登録されています");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }
}
