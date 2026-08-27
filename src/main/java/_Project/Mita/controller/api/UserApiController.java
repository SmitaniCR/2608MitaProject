package _Project.Mita.controller.api;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.entity.User;
import _Project.Mita.exception.NotAuthenticatedException;
import _Project.Mita.exception.SelfDemotionException;
import _Project.Mita.form.UserRoleUpdateRequest;
import _Project.Mita.response.UserResponse;
import _Project.Mita.service.SessionUserService;
import _Project.Mita.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;
    private final SessionUserService sessionUserService;

    public UserApiController(UserService userService, SessionUserService sessionUserService) {
        this.userService = userService;
        this.sessionUserService = sessionUserService;
    }

    /**
     * 削除されていないすべてのユーザーのリストを取得
     *
     * @return ユーザーのリスト
     */
    @GetMapping
    public List<UserResponse> list() {
        return userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    /**
     * 指定されたユーザーの権限を更新
     *
     * @param id 権限を変更する対象のユーザーID
     * @param request 変更後の権限情報を含むリクエスト
     * @param servletRequest セッションを確認するためのHTTPリクエスト
     * @return 更新されたユーザー
     * @throws NotAuthenticatedException ログインされていなかったとき
     * @throws SelfDemotionException ログイン中の管理者が自分自身の管理者権限を外そうとしたとき
     * @throws NoSuchElementException 指定されたIDのユーザーが存在しないとき
     */
    @PatchMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable("id") Long id, @Valid @RequestBody UserRoleUpdateRequest request,
            HttpServletRequest servletRequest) {
        User currentAdmin = sessionUserService.requireCurrentUser(servletRequest);
        return UserResponse.from(userService.updateRole(currentAdmin, id, request.isAdmin()));
    }

    /**
     * 指定されたユーザーを論理削除
     *
     * @param id 削除対象のユーザーID
     * @throws NoSuchElementException 指定されたIDのユーザーが存在しないとき
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        userService.delete(id);
    }
}
