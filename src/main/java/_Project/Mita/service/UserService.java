package _Project.Mita.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.User;
import _Project.Mita.exception.AuthenticationFailedException;
import _Project.Mita.exception.DuplicateEmailException;
import _Project.Mita.form.LoginRequest;
import _Project.Mita.form.UserRegisterRequest;
import _Project.Mita.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findByIsDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByIdOptional(Long userId) {
        return userRepository.findById(userId).filter(user -> !user.isDeleted());
    }

    public User register(UserRegisterRequest request) {
        if (userRepository.existsByEmailAndIsDeletedFalse(request.email())) {
            throw new DuplicateEmailException("このメールアドレスは既に登録されています");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User authenticate(LoginRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.email())
                .orElseThrow(() -> new AuthenticationFailedException("メールアドレスまたはパスワードが正しくありません"));
        if (!user.getPassword().equals(request.password())) {
            throw new AuthenticationFailedException("メールアドレスまたはパスワードが正しくありません");
        }
        return user;
    }
}
