package _Project.Mita.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
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

    /**
     * 削除されていないすべてのユーザーを取得
     *
     * @return 削除されていないユーザーのリスト
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findByIsDeletedFalse();
    }

    /**
     * 指定されたユーザーIDに一致する削除されていないユーザーを検索
     *
     * @param userId 検索対象のユーザーID
     * @return 削除されていないユーザーを含む {@link Optional}、または空の {@link Optional}
     */
    @Transactional(readOnly = true)
    public Optional<User> findByIdOptional(Long userId) {
        return userRepository.findById(userId).filter(user -> !user.isDeleted());
    }

    /**
     * 指定されたユーザーIDのユーザーを論理削除
     *
     * @param userId 削除対象のユーザーID
     * @throws NoSuchElementException 指定されたIDのユーザーが存在しないとき
     */
    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: id=" + userId));
        user.setDeleted(true);
        userRepository.save(user);
    }

    /**
     * 指定されたユーザーの管理者権限を変更
     *
     * @param currentAdmin 現在ログインしている管理者のユーザー情報
     * @param userId 権限を変更する対象のユーザーID
     * @param isAdmin 付与する場合は true、一般ユーザーに降格させる場合は false
     * @return 更新されたユーザー情報
     * @throws SelfDemotionException ログイン中の管理者が自身の管理者権限を外そうとしたとき
     * @throws NoSuchElementException 指定されたIDのユーザーが存在しないとき
     */
    public User updateRole(User currentAdmin, Long userId, boolean isAdmin) {
        if (!isAdmin && currentAdmin.getUserId().equals(userId)) {
            throw new SelfDemotionException("自分自身の管理者権限は変更できません");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: id=" + userId));
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    /**
     * 新しいユーザーをシステムに登録
     *
     * @param request 登録するユーザーの情報を含むリクエスト
     * @return 登録されたユーザー情報
     * @throws DuplicateEmailException メールアドレスが既に登録されているとき
     */
    public User register(UserRegisterRequest request) {
        if (userRepository.existsByEmailAndIsDeletedFalse(request.email())) {
            throw new DuplicateEmailException("このメールアドレスは既に登録されています");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("このメールアドレスは既に登録されています");
        }
    }
}
